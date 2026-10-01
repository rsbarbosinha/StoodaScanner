package com.example.stoodascanner

import com.example.stoodascanner.utils.StudentImportParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class StudentImportParserTest {

    @Test
    fun testIndexToColLetter() {
        assertEquals("A", StudentImportParser.indexToColLetter(0))
        assertEquals("B", StudentImportParser.indexToColLetter(1))
        assertEquals("Z", StudentImportParser.indexToColLetter(25))
        assertEquals("AA", StudentImportParser.indexToColLetter(26))
        assertEquals("AB", StudentImportParser.indexToColLetter(27))
        assertEquals("AZ", StudentImportParser.indexToColLetter(51))
        assertEquals("BA", StudentImportParser.indexToColLetter(52))
    }

    @Test
    fun testParseCsvStreamWithHeader() {
        val csv = """
            Name,Age
            Alice Smith,15
            Bob Jones,16
            Charlie Brown,15
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val names = StudentImportParser.parseCsvStream(inputStream, targetColIndex = 0)

        // "Name" is a single word (no space), so it should be skipped as a header
        assertEquals(listOf("Alice Smith", "Bob Jones", "Charlie Brown"), names)
    }

    @Test
    fun testParseCsvStreamWithoutHeader() {
        val csv = """
            Alice Smith,15
            Bob Jones,16
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val names = StudentImportParser.parseCsvStream(inputStream, targetColIndex = 0)

        // First row "Alice Smith" has a space, so it should be treated as a student name, not a header
        assertEquals(listOf("Alice Smith", "Bob Jones"), names)
    }

    @Test
    fun testParseCsvStreamDifferentColumnIndex() {
        val csv = """
            ID,Name,Grade
            1,Diana Prince,A
            2,Bruce Wayne,A
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val names = StudentImportParser.parseCsvStream(inputStream, targetColIndex = 1)

        assertEquals(listOf("Diana Prince", "Bruce Wayne"), names)
    }

    @Test
    fun testParseCsvStreamIgnoresEmptyLinesAndTrimsWhitespace() {
        val csv = """
            Name
            
               Clark Kent   
            
            Peter Parker
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val names = StudentImportParser.parseCsvStream(inputStream, targetColIndex = 0)

        assertEquals(listOf("Clark Kent", "Peter Parker"), names)
    }

    @Test
    fun testParseCsvStreamLimitsTo64Students() {
        val rows = (1..75).joinToString("\n") { "Student $it" }
        val inputStream = ByteArrayInputStream(rows.toByteArray(StandardCharsets.UTF_8))
        val names = StudentImportParser.parseCsvStream(inputStream, targetColIndex = 0)

        assertEquals(64, names.size)
        assertEquals("Student 1", names.first())
        assertEquals("Student 64", names.last())
    }

    @Test
    fun testParseCsvStreamInvalidColumnReturnsEmpty() {
        val csv = """
            Alice Smith
            Bob Jones
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val names = StudentImportParser.parseCsvStream(inputStream, targetColIndex = 5)

        assertTrue(names.isEmpty())
    }
}
