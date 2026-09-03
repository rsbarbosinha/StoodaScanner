package com.example.stoodascanner.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.stoodascanner.data.AppState
import com.example.stoodascanner.data.ClassManager
import com.example.stoodascanner.data.StudentClass

enum class CreationType {
    IMPORT, CUSTOM
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val classManager = ClassManager(application)

    var appState by mutableStateOf(AppState.SPLASH)
    var isDebugMode by mutableStateOf(false)
    var analysisResolution by mutableStateOf("")

    var selectedClass by mutableStateOf<StudentClass?>(null)
    var editingClass by mutableStateOf<StudentClass?>(null)
    var creationType by mutableStateOf(CreationType.CUSTOM)
    val scannedCodes = mutableStateListOf<String>()
    var targetCount by mutableIntStateOf(0)
    var isScanningFinished by mutableStateOf(false)
    var pendingQrStrings by mutableStateOf<List<String>?>(null)

    fun navigateTo(state: AppState) {
        appState = state
    }

    fun selectClass(studentClass: StudentClass) {
        selectedClass = studentClass
        targetCount = studentClass.students.size
        scannedCodes.clear()
        repeat(targetCount) { scannedCodes.add("") }
        isScanningFinished = false
        appState = AppState.SCANNING
    }

    fun showSetupLayout() {
        appState = AppState.CLASS_SELECTION
        scannedCodes.clear()
        isScanningFinished = false
    }

    fun handleBackPress(onExit: () -> Unit, onDiscard: () -> Unit, onShowSetup: () -> Unit) {
        when (appState) {
            AppState.GRAPH -> appState = AppState.RESULTS
            AppState.RESULTS -> onDiscard()
            AppState.SCANNING -> onShowSetup()
            AppState.CLASS_SELECTION -> onExit()
            AppState.CLASS_CREATION_CHOICE -> appState = AppState.CLASS_SELECTION
            AppState.CLASS_CREATION -> appState = AppState.CLASS_CREATION_CHOICE
            AppState.CLASS_MANAGEMENT -> {
                if (editingClass != null) {
                    editingClass = null
                } else {
                    appState = AppState.CLASS_SELECTION
                }
            }
            AppState.SPLASH -> onExit()
        }
    }
}