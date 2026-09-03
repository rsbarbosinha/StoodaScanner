package com.example.stoodascanner

import com.example.stoodascanner.utils.StudentImportParser
import org.junit.Assert.assertEquals
import org.junit.Test

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
}
