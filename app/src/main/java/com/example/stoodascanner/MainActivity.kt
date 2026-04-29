package com.example.stoodascanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    private lateinit var layoutSplash: LinearLayout
    private lateinit var layoutSetup: LinearLayout
    private lateinit var layoutScanning: RelativeLayout
    private lateinit var layoutResults: LinearLayout
    private lateinit var layoutGraph: LinearLayout
    private lateinit var overlayView: ScanOverlay

    private lateinit var editQrCount: EditText
    private lateinit var tvProgress: TextView
    private lateinit var viewFinder: PreviewView
    private lateinit var listViewResults: ListView
    private lateinit var graphView: ResultGraphView
    private lateinit var progressBarSplash: android.widget.ProgressBar

    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraControl: CameraControl? = null

    private var targetCount = 0
    private val scannedCodes = java.util.TreeSet<String>()

    private var isScanningFinished = false

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 10
        private const val STORAGE_PERMISSION_REQUEST_CODE = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        layoutSplash = findViewById(R.id.layoutSplash)
        layoutSetup = findViewById(R.id.layoutSetup)
        layoutScanning = findViewById(R.id.layoutScanning)
        layoutResults = findViewById(R.id.layoutResults)
        layoutGraph = findViewById(R.id.layoutGraph)
        overlayView = findViewById(R.id.overlayView)

        editQrCount = findViewById(R.id.editQrCount)
        tvProgress = findViewById(R.id.tvProgress)
        viewFinder = findViewById(R.id.viewFinder)
        listViewResults = findViewById(R.id.listViewResults)
        graphView = findViewById(R.id.graphView)
        progressBarSplash = findViewById(R.id.progressBarSplash)

        val btnStartScan = findViewById<Button>(R.id.btnStartScan)
        val btnGenerateQr = findViewById<Button>(R.id.btnGenerateQr)
        val btnRestart = findViewById<Button>(R.id.btnRestart)
        val btnShowGraph = findViewById<Button>(R.id.btnShowGraph)
        val btnRestartFromGraph = findViewById<Button>(R.id.btnRestartFromGraph)

        cameraExecutor = Executors.newSingleThreadExecutor()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    layoutGraph.isVisible -> showResultsLayout()
                    layoutResults.isVisible -> showDiscardResultsDialog()
                    layoutScanning.isVisible -> showSetupLayout()
                    layoutSetup.isVisible -> showExitDialog()
                    else -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })

        btnStartScan.setOnClickListener {
            hideKeyboard()
            val input = editQrCount.text.toString()
            val count = input.toIntOrNull()

            if (count != null && count in 1..64) {
                targetCount = count
                checkPermissionsAndStart()
            } else {
                Toast.makeText(this, "Please enter a number between 1 and 64", Toast.LENGTH_SHORT).show()
            }
        }

        btnGenerateQr.setOnClickListener {
            checkStoragePermissionAndGenerate()
        }

        btnRestart.setOnClickListener {
            showSetupLayout()
        }

        btnShowGraph.setOnClickListener {
            showGraphLayout()
        }

        btnRestartFromGraph.setOnClickListener {
            showSetupLayout()
        }

        showSplashLayout()
    }

    private fun showSplashLayout() {
        layoutSetup.visibility = View.GONE
        layoutScanning.visibility = View.GONE
        layoutResults.visibility = View.GONE
        layoutGraph.visibility = View.GONE
        layoutSplash.visibility = View.VISIBLE

        val totalTime = 3000L
        val interval = 30L
        val steps = (totalTime / interval).toInt()
        
        var currentStep = 0
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        
        val runnable = object : Runnable {
            override fun run() {
                if (currentStep <= steps) {
                    progressBarSplash.progress = (currentStep.toFloat() / steps * 100).toInt()
                    currentStep++
                    handler.postDelayed(this, interval)
                } else {
                    showSetupLayout()
                }
            }
        }
        handler.post(runnable)
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Do you really want to quit?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showDiscardResultsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Discard Results")
            .setMessage("Do you want to discard these results and go back to the main menu?")
            .setPositiveButton("Yes") { _, _ -> showSetupLayout() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startScanningLayout()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkStoragePermissionAndGenerate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            QRGenerator(this).generateStoodaPdf()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanningLayout()
                } else {
                    Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    QRGenerator(this).generateStoodaPdf()
                } else {
                    Toast.makeText(this, "Storage permission is required to save the PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startScanningLayout() {
        scannedCodes.clear()
        isScanningFinished = false
        updateProgressText()

        layoutSetup.visibility = View.GONE
        layoutResults.visibility = View.GONE
        layoutGraph.visibility = View.GONE
        layoutScanning.visibility = View.VISIBLE

        startCamera()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QRCodeAnalyzer(targetCount) { qrText, imageWidth, imageHeight, rawX, rawY ->
                        handleQrCodeFound(qrText, imageWidth, imageHeight, rawX, rawY)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                val camera = cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                cameraControl = camera?.cameraControl

                // Pinch to zoom
                val scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        val zoomState = camera?.cameraInfo?.zoomState?.value
                        val currentZoomRatio = zoomState?.zoomRatio ?: 1f
                        cameraControl?.setZoomRatio(currentZoomRatio * detector.scaleFactor)
                        return true
                    }
                })

                viewFinder.setOnTouchListener { _, event ->
                    scaleGestureDetector.onTouchEvent(event)

                    if (event.action == MotionEvent.ACTION_UP) {
                        // Tap to focus
                        val factory = viewFinder.meteringPointFactory
                        val point = factory.createPoint(event.x, event.y)
                        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                        cameraControl?.startFocusAndMetering(action)
                    }
                    true
                }

            } catch (_: Exception) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun handleQrCodeFound(qrText: String, imageWidth: Int, imageHeight: Int, rawX: Int, rawY: Int) {
        if (isScanningFinished) return

        // Validate: must be exactly 4 digits
        if (!qrText.matches(Regex("\\d{4}"))) {
            return
        }

        // Extract values
        val firstDigit = qrText.substring(0, 1).toInt()
        val secondDigit = qrText.substring(1, 2).toInt()
        val thirdDigit = qrText.substring(2, 3).toInt()
        val forthDigit = qrText.substring(3, 4).toInt()
        val firstTwo = qrText.substring(0, 2).toInt()

        // Validate ranges
        if (firstTwo !in 0..63 || thirdDigit !in 0..5 ||
            forthDigit != (firstDigit+secondDigit+thirdDigit) % 10) {
            return
        }
        // Run on UI thread since Analyzer runs on a background thread
        runOnUiThread {
            if (isScanningFinished || scannedCodes.size >= targetCount) return@runOnUiThread

            // Calculate scale factors. Note: If in portrait, swap imageWidth and imageHeight
            val scaleX = viewFinder.width.toFloat() / imageHeight.toFloat()
            val scaleY = viewFinder.height.toFloat() / imageWidth.toFloat()

            // Apply scaling to find the exact screen coordinate
            val screenX = rawX * scaleX
            val screenY = rawY * scaleY

            overlayView.addPoint(screenX, screenY)

            // .add() returns true if the item was not already in the set (meaning it's unique)
            if (scannedCodes.add(qrText)) {
                triggerHapticFeedback()
                updateProgressText()

                if (scannedCodes.size >= targetCount) {
                    finishScanning()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgressText() {
        tvProgress.text = "Scanned: ${scannedCodes.size} / $targetCount"
    }

    private fun finishScanning() {
        if (isScanningFinished) return
        isScanningFinished = true

        cameraProvider?.unbindAll()
        showResultsLayout()
    }

    private fun showResultsLayout() {
        layoutSetup.visibility = View.GONE
        layoutScanning.visibility = View.GONE
        layoutGraph.visibility = View.GONE
        layoutResults.visibility = View.VISIBLE

        val decoder = QRDecoder()
        val decodedList = scannedCodes.map { decoder.decode(it) }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, decodedList)
        listViewResults.adapter = adapter
    }

    private fun showGraphLayout() {
        layoutSetup.visibility = View.GONE
        layoutScanning.visibility = View.GONE
        layoutResults.visibility = View.GONE
        layoutGraph.visibility = View.VISIBLE

        val decoder = QRDecoder()
        val counts = mutableMapOf<String, Int>()
        
        // Populate counts for A, B, C, D, E, ?
        scannedCodes.forEach { code ->
            val decoded = decoder.decode(code)
            // Extract the "Type" part (the A, B, C etc. after the "-")
            val type = decoded.split(" - ").getOrNull(1) ?: "?"
            counts[type] = (counts[type] ?: 0) + 1
        }

        graphView.setData(counts)
    }

    private fun showSetupLayout() {
        layoutSplash.visibility = View.GONE
        layoutScanning.visibility = View.GONE
        layoutResults.visibility = View.GONE
        layoutGraph.visibility = View.GONE
        layoutSetup.visibility = View.VISIBLE
        editQrCount.text.clear()
        scannedCodes.clear()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun triggerHapticFeedback() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
