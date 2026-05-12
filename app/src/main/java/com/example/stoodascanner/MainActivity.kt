package com.example.stoodascanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.camera.view.PreviewView
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 10
        private const val STORAGE_PERMISSION_REQUEST_CODE = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }

    @Composable
    fun MainScreen() {
        val navController = rememberNavController()

        BackHandler {
            viewModel.handleBackPress(
                onDiscard = { showDiscardResultsDialog() },
                onExit = { showExitDialog() },
                onShowSetup = { viewModel.showSetupLayout() }
            )
        }

        // Sink ViewModel appState to NavController
        androidx.compose.runtime.LaunchedEffect(viewModel.appState) {
            navController.navigate(viewModel.appState.name) {
                // Pop up to the menu or splash to avoid deep stacks if needed
                if (viewModel.appState == AppState.MENU) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }

        NavHost(navController = navController, startDestination = viewModel.appState.name) {
            composable(AppState.SPLASH.name) {
                SplashScreen { viewModel.navigateTo(AppState.MENU) }
            }
            composable(AppState.MENU.name) {
                MenuScreen(
                    onNavigate = { viewModel.navigateTo(it) },
                    onExit = { showExitDialog() }
                )
            }
            composable(AppState.CLASS_CREATION.name) {
                ClassCreationScreen(
                    viewModel = viewModel,
                    onGeneratePdf = { studentNames -> checkStoragePermissionAndGenerate(studentNames) }
                )
            }
            composable(AppState.CLASS_MANAGEMENT.name) {
                ClassManagementScreen(
                    viewModel = viewModel,
                    onGeneratePdf = { studentNames -> checkStoragePermissionAndGenerate(studentNames) }
                )
            }
            composable(AppState.SETUP.name) {
                SetupScreen(
                    viewModel = viewModel,
                    onStartScan = { checkPermissionsAndStart() }
                )
            }
            composable(AppState.SCANNING.name) {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current

                ScanningScreen(
                    scannedCount = viewModel.scannedCodes.size,
                    targetCount = viewModel.targetCount,
                    onStartCamera = { previewView, onAddPoint ->
                        val cameraManager = CameraManager(
                            context = context,
                            previewView = previewView,
                            lifecycleOwner = lifecycleOwner,
                            onQrCodeScanned = { qrText, imageWidth, imageHeight, rawX, rawY ->
                                handleQrCodeFound(qrText, imageWidth, imageHeight, rawX, rawY, previewView, onAddPoint)
                            }
                        )
                        cameraManager.startCamera(viewModel.targetCount)
                        cameraManager
                    }
                )
            }
            composable(AppState.RESULTS.name) {
                ResultsScreen(
                    scannedCodes = viewModel.scannedCodes,
                    selectedClass = viewModel.selectedClass,
                    onShowGraph = { viewModel.navigateTo(AppState.GRAPH) },
                    onRestart = { viewModel.showSetupLayout() }
                )
            }
            composable(AppState.GRAPH.name) {
                GraphScreen(
                    scannedCodes = viewModel.scannedCodes,
                    selectedClass = viewModel.selectedClass,
                    onRestart = { viewModel.showSetupLayout() }
                )
            }
        }
    }

    private fun handleQrCodeFound(qrText: String, imageWidth: Int, imageHeight: Int, rawX: Int, rawY: Int, previewView: PreviewView, onAddPoint: (Float, Float) -> Unit) {
        if (viewModel.isScanningFinished) return
        if (!qrText.matches(Regex("\\d{4}"))) return

        val firstDigit = qrText.substring(0, 1).toInt()
        val secondDigit = qrText.substring(1, 2).toInt()
        val thirdDigit = qrText.substring(2, 3).toInt()
        val forthDigit = qrText.substring(3, 4).toInt()
        val firstTwo = qrText.substring(0, 2).toInt()

        if (firstTwo !in 0..63 || thirdDigit !in 0..5 || forthDigit != (firstDigit+secondDigit+thirdDigit) % 10) return

        runOnUiThread {
            if (viewModel.isScanningFinished || viewModel.scannedCodes.size >= viewModel.targetCount) return@runOnUiThread

            val scaleX = previewView.width.toFloat() / imageHeight.toFloat()
            val scaleY = previewView.height.toFloat() / imageWidth.toFloat()
            val screenX = rawX * scaleX
            val screenY = rawY * scaleY

            onAddPoint(screenX, screenY)

            val studentPrefix = qrText.substring(0, 2)
            val existingIndex = viewModel.scannedCodes.indexOfFirst { it.startsWith(studentPrefix) }

            if (existingIndex == -1) {
                // New student scanned
                if (viewModel.scannedCodes.size < viewModel.targetCount) {
                    viewModel.scannedCodes.add(qrText)
                    triggerHapticFeedback()
                    if (viewModel.scannedCodes.size >= viewModel.targetCount) {
                        finishScanning()
                    }
                }
            } else if (viewModel.scannedCodes[existingIndex] != qrText) {
                // Same student, different answer - update it
                viewModel.scannedCodes[existingIndex] = qrText
                triggerHapticFeedback()
            }
        }
    }

    private fun finishScanning() {
        if (viewModel.isScanningFinished) return
        viewModel.isScanningFinished = true
        viewModel.navigateTo(AppState.RESULTS)
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this).setTitle("Exit App").setMessage("Do you really want to quit?")
            .setPositiveButton("Yes") { _, _ -> finish() }.setNegativeButton("No", null).show()
    }

    private fun showDiscardResultsDialog() {
        AlertDialog.Builder(this).setTitle("Discard Results").setMessage("Do you want to discard these results and go back to the main menu?")
            .setPositiveButton("Yes") { _, _ -> viewModel.showSetupLayout() }.setNegativeButton("No", null).show()
    }

    private fun checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            viewModel.scannedCodes.clear()
            viewModel.isScanningFinished = false
            viewModel.navigateTo(AppState.SCANNING)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkStoragePermissionAndGenerate(studentNames: List<String>? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (studentNames != null) {
                QRGenerator(this).generateStoodaPdf(studentNames)
            } else {
                // Default fallback if somehow called without names
                QRGenerator(this).generateStoodaPdf(emptyList())
            }
        } else {
            viewModel.pendingQrStrings = studentNames
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
            checkPermissionsAndStart()
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
            checkStoragePermissionAndGenerate(viewModel.pendingQrStrings)
            viewModel.pendingQrStrings = null
        }
    }

    private fun triggerHapticFeedback() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(100)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
