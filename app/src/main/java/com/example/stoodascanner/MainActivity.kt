package com.example.stoodascanner

import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var layoutSetup: LinearLayout
    private lateinit var layoutScanning: RelativeLayout
    private lateinit var layoutResults: LinearLayout

    private lateinit var editQrCount: EditText
    private lateinit var tvProgress: TextView
    private lateinit var viewFinder: PreviewView
    private lateinit var listViewResults: ListView

    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null

    private var targetCount = 0
    private val scannedCodes = mutableSetOf<String>()

    private var isScanningFinished = false // Add this flag

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        layoutSetup = findViewById(R.id.layoutSetup)
        layoutScanning = findViewById(R.id.layoutScanning)
        layoutResults = findViewById(R.id.layoutResults)

        editQrCount = findViewById(R.id.editQrCount)
        tvProgress = findViewById(R.id.tvProgress)
        viewFinder = findViewById(R.id.viewFinder)
        listViewResults = findViewById(R.id.listViewResults)

        val btnStartScan = findViewById<Button>(R.id.btnStartScan)
        val btnRestart = findViewById<Button>(R.id.btnRestart)

        cameraExecutor = Executors.newSingleThreadExecutor()

        btnStartScan.setOnClickListener {
            val input = editQrCount.text.toString()
            val count = input.toIntOrNull()

            if (count != null && count in 1..64) {
                targetCount = count
                checkPermissionsAndStart()
            } else {
                Toast.makeText(this, "Please enter a number between 1 and 64", Toast.LENGTH_SHORT).show()
            }
        }

        btnRestart.setOnClickListener {
            showSetupLayout()
        }

        showSetupLayout()
    }

    private fun checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startScanningLayout()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanningLayout()
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startScanningLayout() {
        scannedCodes.clear()
        isScanningFinished = false // Reset here
        updateProgressText()

        layoutSetup.visibility = View.GONE
        layoutResults.visibility = View.GONE
        layoutScanning.visibility = View.VISIBLE

        startCamera()
    }

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
                    it.setAnalyzer(cameraExecutor, QRCodeAnalyzer(targetCount) { qrText ->
                        handleQrCodeFound(qrText)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun handleQrCodeFound(qrText: String) {
        // Validate: must be exactly 3 digits
        if (!qrText.matches(Regex("\\d{3}"))) {
            return
        }

        // Extract values
        val firstTwo = qrText.substring(0, 2).toInt()
        val lastDigit = qrText.substring(2, 3).toInt()

        // Validate ranges
        if (firstTwo !in 1..64 || lastDigit !in 0..5) {
            return
        }
        // Run on UI thread since Analyzer runs on a background thread
        runOnUiThread {
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

    private fun updateProgressText() {
        tvProgress.text = "Scanned: ${scannedCodes.size} / $targetCount"
    }

    private fun finishScanning() {
        if (isScanningFinished) return // Prevent multiple triggers
        isScanningFinished = true

        cameraProvider?.unbindAll() // Stop the camera
        showResultsLayout()
    }

    private fun showResultsLayout() {
        layoutSetup.visibility = View.GONE
        layoutScanning.visibility = View.GONE
        layoutResults.visibility = View.VISIBLE

        // Populate ListView with results
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, scannedCodes.toList())
        listViewResults.adapter = adapter
    }

    private fun showSetupLayout() {
        layoutScanning.visibility = View.GONE
        layoutResults.visibility = View.GONE
        layoutSetup.visibility = View.VISIBLE
        editQrCount.text.clear()
        scannedCodes.clear()
    }

    private fun triggerHapticFeedback() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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