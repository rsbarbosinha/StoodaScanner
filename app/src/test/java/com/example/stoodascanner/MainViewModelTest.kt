package com.example.stoodascanner

import android.app.Application
import com.example.stoodascanner.data.AppState
import com.example.stoodascanner.data.StudentClass
import com.example.stoodascanner.viewModel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class MainViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var application: Application
    private lateinit var viewModel: MainViewModel

    private val testClass = StudentClass(
        title = "Science 101",
        nickname = "SCI",
        color = 0xFF2196F3.toInt(),
        students = listOf("Alice", "Bob", "Charlie")
    )

    @Before
    fun setUp() {
        application = mock(Application::class.java)
        `when`(application.filesDir).thenReturn(tempFolder.root)
        viewModel = MainViewModel(application)
    }

    @Test
    fun testInitialState() {
        assertEquals(AppState.SPLASH, viewModel.appState)
        assertFalse(viewModel.isDebugMode)
        assertNull(viewModel.selectedClass)
        assertNull(viewModel.activeSessionClass)
        assertTrue(viewModel.presentStudentIndices.isEmpty())
        assertFalse(viewModel.attendanceTaken)
        assertFalse(viewModel.isScanningFinished)
    }

    @Test
    fun testNavigateTo() {
        viewModel.navigateTo(AppState.CLASS_SELECTION)
        assertEquals(AppState.CLASS_SELECTION, viewModel.appState)
    }

    @Test
    fun testSelectClassInitializesSession() {
        viewModel.selectClass(testClass)

        assertEquals(testClass, viewModel.selectedClass)
        assertEquals(testClass, viewModel.activeSessionClass)
        assertEquals(listOf(0, 1, 2), viewModel.presentStudentIndices.toList())
        assertFalse(viewModel.attendanceTaken)
        assertEquals(AppState.SESSION_OPTIONS, viewModel.appState)
    }

    @Test
    fun testStartAttendanceCheck() {
        viewModel.selectClass(testClass)
        viewModel.startAttendanceCheck()

        assertEquals(AppState.ATTENDANCE_SCANNING, viewModel.appState)
        assertTrue(viewModel.attendanceTaken)
        assertTrue(viewModel.presentStudentIndices.isEmpty())
        assertEquals(3, viewModel.scannedCodes.size)
        assertTrue(viewModel.scannedCodes.all { it.isEmpty() })
        assertFalse(viewModel.isScanningFinished)
    }

    @Test
    fun testStartQuizSetsTargetCountAndCodes() {
        viewModel.selectClass(testClass)
        // Simulate only students 0 and 2 present
        viewModel.presentStudentIndices.clear()
        viewModel.presentStudentIndices.addAll(listOf(0, 2))

        viewModel.startQuiz()

        assertEquals(AppState.SCANNING, viewModel.appState)
        assertEquals(2, viewModel.targetCount)
        assertEquals(3, viewModel.scannedCodes.size)
        assertTrue(viewModel.scannedCodes.all { it.isEmpty() })
        assertFalse(viewModel.isScanningFinished)
    }

    @Test
    fun testFinishSession() {
        viewModel.selectClass(testClass)
        viewModel.finishSession()

        assertNull(viewModel.activeSessionClass)
        assertTrue(viewModel.presentStudentIndices.isEmpty())
        assertFalse(viewModel.attendanceTaken)
        assertEquals(AppState.CLASS_SELECTION, viewModel.appState)
    }

    @Test
    fun testShowSetupLayout() {
        viewModel.scannedCodes.add("0000")
        viewModel.isScanningFinished = true

        viewModel.showSetupLayout()

        assertEquals(AppState.CLASS_SELECTION, viewModel.appState)
        assertTrue(viewModel.scannedCodes.isEmpty())
        assertFalse(viewModel.isScanningFinished)
    }

    @Test
    fun testHandleBackPressFromGraph() {
        viewModel.navigateTo(AppState.GRAPH)
        var exitCalled = false
        var discardCalled = false

        viewModel.handleBackPress(onExit = { exitCalled = true }, onDiscard = { discardCalled = true })

        assertEquals(AppState.RESULTS, viewModel.appState)
        assertFalse(exitCalled)
        assertFalse(discardCalled)
    }

    @Test
    fun testHandleBackPressFromResultsCallsDiscard() {
        viewModel.navigateTo(AppState.RESULTS)
        var exitCalled = false
        var discardCalled = false

        viewModel.handleBackPress(onExit = { exitCalled = true }, onDiscard = { discardCalled = true })

        assertTrue(discardCalled)
        assertFalse(exitCalled)
    }

    @Test
    fun testHandleBackPressFromScanningNavigatesToSessionOptions() {
        viewModel.navigateTo(AppState.SCANNING)
        viewModel.handleBackPress(onExit = {}, onDiscard = {})
        assertEquals(AppState.SESSION_OPTIONS, viewModel.appState)

        viewModel.navigateTo(AppState.ATTENDANCE_SCANNING)
        viewModel.handleBackPress(onExit = {}, onDiscard = {})
        assertEquals(AppState.SESSION_OPTIONS, viewModel.appState)
    }

    @Test
    fun testHandleBackPressFromSessionOptionsNavigatesToClassSelection() {
        viewModel.navigateTo(AppState.SESSION_OPTIONS)
        viewModel.handleBackPress(onExit = {}, onDiscard = {})
        assertEquals(AppState.CLASS_SELECTION, viewModel.appState)
    }

    @Test
    fun testHandleBackPressFromClassSelectionCallsExit() {
        viewModel.navigateTo(AppState.CLASS_SELECTION)
        var exitCalled = false
        viewModel.handleBackPress(onExit = { exitCalled = true }, onDiscard = {})
        assertTrue(exitCalled)
    }

    @Test
    fun testHandleBackPressFromCreationFlow() {
        viewModel.navigateTo(AppState.CLASS_CREATION_CHOICE)
        viewModel.handleBackPress(onExit = {}, onDiscard = {})
        assertEquals(AppState.CLASS_SELECTION, viewModel.appState)

        viewModel.navigateTo(AppState.CLASS_CREATION)
        viewModel.handleBackPress(onExit = {}, onDiscard = {})
        assertEquals(AppState.CLASS_CREATION_CHOICE, viewModel.appState)
    }

    @Test
    fun testHandleBackPressFromClassManagement() {
        // Case 1: editingClass with active session
        viewModel.navigateTo(AppState.CLASS_MANAGEMENT)
        viewModel.editingClass = testClass
        viewModel.activeSessionClass = testClass
        viewModel.handleBackPress(onExit = {}, onDiscard = {})
        assertNull(viewModel.editingClass)
        assertEquals(AppState.SESSION_OPTIONS, viewModel.appState)

        // Case 2: editingClass without active session
        viewModel.navigateTo(AppState.CLASS_MANAGEMENT)
        viewModel.editingClass = testClass
        viewModel.activeSessionClass = null
        viewModel.handleBackPress(onExit = {}, onDiscard = {})
        assertNull(viewModel.editingClass)
        assertEquals(AppState.CLASS_SELECTION, viewModel.appState)

        // Case 3: no editingClass
        viewModel.navigateTo(AppState.CLASS_MANAGEMENT)
        viewModel.editingClass = null
        viewModel.handleBackPress(onExit = {}, onDiscard = {})
        assertEquals(AppState.CLASS_SELECTION, viewModel.appState)
    }

    @Test
    fun testHandleBackPressFromSplashCallsExit() {
        viewModel.navigateTo(AppState.SPLASH)
        var exitCalled = false
        viewModel.handleBackPress(onExit = { exitCalled = true }, onDiscard = {})
        assertTrue(exitCalled)
    }
}
