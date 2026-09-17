package com.example.stoodascanner

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
    var analysisResolution by mutableStateOf("")
    
    var selectedClass by mutableStateOf<StudentClass?>(null)
    var activeSessionClass by mutableStateOf<StudentClass?>(null)
    val presentStudentIndices = mutableStateListOf<Int>()
    var attendanceTaken by mutableStateOf(false)
    
    var editingClass by mutableStateOf<StudentClass?>(null)
    val scannedCodes = mutableStateListOf<String>()
    var targetCount by mutableIntStateOf(0)
    var isScanningFinished by mutableStateOf(false)
    var pendingQrStrings by mutableStateOf<List<String>?>(null)

    fun navigateTo(state: AppState) {
        appState = state
    }

    fun selectClass(studentClass: StudentClass) {
        selectedClass = studentClass
        activeSessionClass = studentClass
        presentStudentIndices.clear()
        val students = studentClass.students ?: emptyList()
        students.indices.forEach { presentStudentIndices.add(it) }
        attendanceTaken = false
        appState = AppState.SESSION_OPTIONS
    }

    fun startAttendanceCheck() {
        val currentClass = activeSessionClass ?: return
        val students = currentClass.students ?: emptyList()
        scannedCodes.clear()
        repeat(students.size) { scannedCodes.add("") }
        presentStudentIndices.clear()
        attendanceTaken = true
        isScanningFinished = false
        appState = AppState.ATTENDANCE_SCANNING
    }

    fun startQuiz() {
        val currentClass = activeSessionClass ?: return
        val students = currentClass.students ?: emptyList()
        selectedClass = currentClass
        targetCount = presentStudentIndices.size
        scannedCodes.clear()
        repeat(students.size) { scannedCodes.add("") }
        isScanningFinished = false
        appState = AppState.SCANNING
    }

    fun finishSession() {
        activeSessionClass = null
        presentStudentIndices.clear()
        attendanceTaken = false
        appState = AppState.CLASS_SELECTION
    }

    fun showSetupLayout() {
        appState = AppState.CLASS_SELECTION
        scannedCodes.clear()
        isScanningFinished = false
    }

    fun handleBackPress(onExit: () -> Unit, onDiscard: () -> Unit) {
        when (appState) {
            AppState.GRAPH -> appState = AppState.RESULTS
            AppState.RESULTS -> onDiscard()
            AppState.SCANNING -> appState = AppState.SESSION_OPTIONS
            AppState.ATTENDANCE_SCANNING -> appState = AppState.SESSION_OPTIONS
            AppState.SESSION_OPTIONS -> appState = AppState.CLASS_SELECTION
            AppState.CLASS_SELECTION -> onExit()
            AppState.CLASS_CREATION -> appState = AppState.CLASS_SELECTION
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
