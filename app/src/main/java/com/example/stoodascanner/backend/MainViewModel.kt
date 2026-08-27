package com.example.stoodascanner.backend

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val classManager = ClassManager(application)
    
    var appState by mutableStateOf(AppState.SPLASH)
    var isDebugMode by mutableStateOf(false)
    var analysisResolution by mutableStateOf("") // Will be set to localized string if needed, or handled in UI
    
    var selectedClass by mutableStateOf<StudentClass?>(null)
    var editingClass by mutableStateOf<StudentClass?>(null)
    val scannedCodes = mutableStateListOf<String>()
    var targetCount by mutableIntStateOf(0)
    var isScanningFinished by mutableStateOf(false)
    var pendingQrStrings by mutableStateOf<List<String>?>(null)

    fun navigateTo(state: AppState) {
        appState = state
    }

    fun selectClass(studentClass: StudentClass?) {
        selectedClass = studentClass
    }

    fun startScanning(count: Int) {
        selectedClass = null
        targetCount = count
        scannedCodes.clear()
        repeat(targetCount) { scannedCodes.add("") }
        isScanningFinished = false
        appState = AppState.SCANNING
    }

    fun startScanningWithClass(studentClass: StudentClass) {
        selectedClass = studentClass
        targetCount = studentClass.students.size
        scannedCodes.clear()
        repeat(targetCount) { scannedCodes.add("") }
        isScanningFinished = false
        appState = AppState.SCANNING
    }

    fun showQuickSetup() {
        appState = AppState.QUICK_SETUP
        selectedClass = null
        scannedCodes.clear()
        isScanningFinished = false
    }

    fun showSelectClass() {
        appState = AppState.SELECT_CLASS
        selectedClass = null
        scannedCodes.clear()
        isScanningFinished = false
    }

    fun showSetupLayout() {
        if (selectedClass != null) {
            appState = AppState.SELECT_CLASS
        } else {
            appState = AppState.QUICK_SETUP
        }
        scannedCodes.clear()
        isScanningFinished = false
    }

    fun handleBackPress(onExit: () -> Unit, onDiscard: () -> Unit, onShowSetup: () -> Unit) {
        when (appState) {
            AppState.GRAPH -> appState = AppState.RESULTS
            AppState.RESULTS -> onDiscard()
            AppState.SCANNING -> onShowSetup()
            AppState.QUICK_SETUP -> appState = AppState.SELECT_CLASS
            AppState.SELECT_CLASS -> onExit()
            AppState.CLASS_CREATION -> appState = AppState.SELECT_CLASS
            AppState.CLASS_MANAGEMENT -> {
                if (editingClass != null) {
                    editingClass = null
                } else {
                    appState = AppState.SELECT_CLASS
                }
            }
            AppState.MENU -> onExit()
            AppState.SPLASH -> onExit()
        }
    }
}
