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
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stoodascanner.data.AppState
import com.example.stoodascanner.scanner.CameraManager
import com.example.stoodascanner.ui.screens.ClassCreationScreen
import com.example.stoodascanner.ui.screens.ClassManagementScreen
import com.example.stoodascanner.ui.screens.ClassSelectionScreen
import com.example.stoodascanner.ui.screens.GraphScreen
import com.example.stoodascanner.ui.screens.ResultsScreen
import com.example.stoodascanner.ui.screens.ScanningScreen
import com.example.stoodascanner.ui.screens.SplashScreen
import com.example.stoodascanner.ui.theme.StoodaScannerTheme
import com.example.stoodascanner.utils.QRGenerator
import com.example.stoodascanner.viewModel.MainViewModel

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 10
        private const val STORAGE_PERMISSION_REQUEST_CODE = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoodaScannerTheme {
                Surface(
                    modifier = Modifier.safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    @Composable
    fun MainScreen() {
        val navController = rememberNavController()

        // Sink ViewModel appState to NavController
        androidx.compose.runtime.LaunchedEffect(viewModel.appState) {
            val destination = viewModel.appState.name
            if (navController.currentDestination?.route != destination) {
                navController.navigate(destination) {
                    // Force the backstack to stay at size 1 by always replacing the current destination.
                    // This prevents the NavController from intercepting back button events,
                    // ensuring our custom BackHandler is the one that executes.
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        NavHost(navController = navController, startDestination = AppState.SPLASH.name) {
            composable(AppState.SPLASH.name) {
                SplashScreen { viewModel.navigateTo(AppState.CLASS_SELECTION) }
            }
            composable(AppState.CLASS_SELECTION.name) {
                ClassSelectionScreen(
                    viewModel = viewModel,
                    onStartScan = { checkPermissionsAndStart() }
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
            composable(AppState.SCANNING.name) {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current

                val allStudents = viewModel.selectedClass?.students ?: emptyList()
                val missingStudents = allStudents.mapIndexedNotNull { index, name ->
                    if (viewModel.scannedCodes.getOrNull(index)?.isEmpty() == true) {
                        if (name.isEmpty()) "ID: ${index + 1}" else name
                    } else null
                }

                ScanningScreen(
                    scannedCount = viewModel.scannedCodes.count { it.isNotEmpty() },
                    targetCount = viewModel.targetCount,
                    isDebugMode = viewModel.isDebugMode,
                    analysisResolution = viewModel.analysisResolution,
                    missingStudents = missingStudents,
                    onFinish = { finishScanning() },
                    onStartCamera = { previewView, onAddPoint ->
                        val cameraManager = CameraManager(
                            context = context,
                            previewView = previewView,
                            lifecycleOwner = lifecycleOwner,
                            onQrCodeScanned = { qrText, imageWidth, imageHeight, rawX, rawY ->
                                handleQrCodeFound(
                                    qrText,
                                    imageWidth,
                                    imageHeight,
                                    rawX,
                                    rawY,
                                    previewView,
                                    onAddPoint
                                )
                            },
                            onResolutionUpdate = { res ->
                                runOnUiThread {
                                    viewModel.analysisResolution = res
                                }
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

        // Handle back press manually via ViewModel to keep state and UI in sync
        BackHandler {
            viewModel.handleBackPress(
                onDiscard = { showDiscardResultsDialog() },
                onExit = { showExitDialog() },
                onShowSetup = { viewModel.showSetupLayout() }
            )
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
            if (viewModel.isScanningFinished) return@runOnUiThread

            val scaleX = previewView.width.toFloat() / imageHeight.toFloat()
            val scaleY = previewView.height.toFloat() / imageWidth.toFloat()
            val screenX = rawX * scaleX
            val screenY = rawY * scaleY

            onAddPoint(screenX, screenY)

            if (firstTwo in viewModel.scannedCodes.indices) {
                val existingCode = viewModel.scannedCodes[firstTwo]
                if (existingCode != qrText) {
                    val wasEmpty = existingCode.isEmpty()
                    viewModel.scannedCodes[firstTwo] = qrText
                    triggerHapticFeedback()

                    if (wasEmpty) {
                        val currentScannedCount = viewModel.scannedCodes.count { it.isNotEmpty() }
                        if (currentScannedCount >= viewModel.targetCount) {
                            finishScanning()
                        }
                    }
                }
            }
        }
    }

    private fun finishScanning() {
        if (viewModel.isScanningFinished) return
        viewModel.isScanningFinished = true
        viewModel.navigateTo(AppState.RESULTS)
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.exit_app_title))
            .setMessage(getString(R.string.exit_app_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> finish() }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun showDiscardResultsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.discard_results_title))
            .setMessage(getString(R.string.discard_results_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> viewModel.showSetupLayout() }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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
