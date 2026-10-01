package com.example.stoodascanner

import android.content.Context
import com.example.stoodascanner.data.ClassManager
import com.example.stoodascanner.data.StudentClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.File

class ClassManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var classManager: ClassManager

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        `when`(context.filesDir).thenReturn(tempFolder.root)
        classManager = ClassManager(context)
    }

    @Test
    fun testGetAllClassesInitiallyEmpty() {
        val classes = classManager.getAllClasses()
        assertTrue(classes.isEmpty())
    }

    @Test
    fun testSaveAndGetClass() {
        val studentClass = StudentClass(
            title = "Math 101",
            nickname = "M101",
            color = 0xFF4CAF50.toInt(),
            students = listOf("Alice", "Bob")
        )

        classManager.saveClass(studentClass)

        val retrieved = classManager.getClass("Math 101")
        assertNotNull(retrieved)
        assertEquals("Math 101", retrieved?.title)
        assertEquals("M101", retrieved?.nickname)
        assertEquals(0xFF4CAF50.toInt(), retrieved?.color)
        assertEquals(listOf("Alice", "Bob"), retrieved?.students)

        val allClasses = classManager.getAllClasses()
        assertEquals(1, allClasses.size)
        assertEquals("Math 101", allClasses[0].title)
    }

    @Test
    fun testSaveClassWithSameTitleUpdatesInsteadOfDuplicating() {
        val classV1 = StudentClass(
            title = "Science",
            nickname = "SCI-1",
            students = listOf("Charlie")
        )
        val classV2 = StudentClass(
            title = "Science",
            nickname = "SCI-Updated",
            students = listOf("Charlie", "David")
        )

        classManager.saveClass(classV1)
        classManager.saveClass(classV2)

        val allClasses = classManager.getAllClasses()
        assertEquals(1, allClasses.size)
        assertEquals("SCI-Updated", allClasses[0].nickname)
        assertEquals(listOf("Charlie", "David"), allClasses[0].students)
    }

    @Test
    fun testDeleteClass() {
        val classA = StudentClass(title = "Class A", nickname = "A")
        val classB = StudentClass(title = "Class B", nickname = "B")

        classManager.saveClass(classA)
        classManager.saveClass(classB)
        assertEquals(2, classManager.getAllClasses().size)

        classManager.deleteClass("Class A")

        val remaining = classManager.getAllClasses()
        assertEquals(1, remaining.size)
        assertEquals("Class B", remaining[0].title)
        assertNull(classManager.getClass("Class A"))
    }

    @Test
    fun testCorruptedJsonReturnsEmptyListGracefully() {
        val file = File(tempFolder.root, "classes.json")
        file.writeText("{ this is definitely not valid json }")

        val result = classManager.getAllClasses()
        assertTrue(result.isEmpty())
    }
}
