package com.example.stoodascanner

import com.example.stoodascanner.scanner.StoodaProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class StoodaProtocolTest {

    @Test
    fun testChecksumCalculation() {
        assertEquals(0, StoodaProtocol.calculateChecksum(0, 0, 0))
        assertEquals(3, StoodaProtocol.calculateChecksum(1, 2, 0))
        assertEquals(4, StoodaProtocol.calculateChecksum(6, 3, 5)) // (6+3+5)%10 = 14%10 = 4
        assertEquals(9, StoodaProtocol.calculateChecksum(3, 3, 3))
        assertEquals(7, StoodaProtocol.calculateChecksum(9, 9, 9)) // (9+9+9)%10 = 27%10 = 7
    }

    @Test
    fun testEncodeKnownValues() {
        assertEquals("0000", StoodaProtocol.encode(0, 0))
        assertEquals("0112", StoodaProtocol.encode(1, 1))
        assertEquals("0224", StoodaProtocol.encode(2, 2))
        assertEquals("1203", StoodaProtocol.encode(12, 0))
        assertEquals("1214", StoodaProtocol.encode(12, 1))
        assertEquals("1225", StoodaProtocol.encode(12, 2))
        assertEquals("1236", StoodaProtocol.encode(12, 3))
        assertEquals("1247", StoodaProtocol.encode(12, 4))
        assertEquals("1258", StoodaProtocol.encode(12, 5))
        assertEquals("6354", StoodaProtocol.encode(63, 5))
    }

    @Test
    fun testEncodeThrowsOnInvalidStudentIndex() {
        try {
            StoodaProtocol.encode(-1, 0)
            fail("Expected IllegalArgumentException for negative student index")
        } catch (_: IllegalArgumentException) {}

        try {
            StoodaProtocol.encode(64, 0)
            fail("Expected IllegalArgumentException for student index >= 64")
        } catch (_: IllegalArgumentException) {}
    }

    @Test
    fun testEncodeThrowsOnInvalidAnswerType() {
        try {
            StoodaProtocol.encode(0, -1)
            fail("Expected IllegalArgumentException for negative answer type")
        } catch (_: IllegalArgumentException) {}

        try {
            StoodaProtocol.encode(0, 6)
            fail("Expected IllegalArgumentException for answer type > 5")
        } catch (_: IllegalArgumentException) {}
    }

    @Test
    fun testAllPossibleValidCodesAreConsistent() {
        // There are 64 students (0..63) and 6 answer types (0..5) = 384 valid combinations
        for (studentIndex in 0 until StoodaProtocol.MAX_STUDENTS) {
            for (type in 0..5) {
                val encoded = StoodaProtocol.encode(studentIndex, type)
                assertTrue("Code $encoded should be valid", StoodaProtocol.isValid(encoded))

                val parsed = StoodaProtocol.parse(encoded)
                assertNotNull("Parsed code should not be null for $encoded", parsed)
                assertEquals(studentIndex, parsed!!.studentIndex)
                assertEquals(type, parsed.answerType)
                assertEquals(StoodaProtocol.ANSWER_LETTERS[type], parsed.answerLetter)
                assertEquals(encoded, parsed.rawCode)
            }
        }
    }

    @Test
    fun testIsValidRejectsMalformedStrings() {
        assertFalse(StoodaProtocol.isValid(""))
        assertFalse(StoodaProtocol.isValid("0"))
        assertFalse(StoodaProtocol.isValid("00"))
        assertFalse(StoodaProtocol.isValid("000"))
        assertFalse(StoodaProtocol.isValid("00000"))
        assertFalse(StoodaProtocol.isValid("ABCD"))
        assertFalse(StoodaProtocol.isValid("01A3"))
        assertFalse(StoodaProtocol.isValid(" 123"))
        assertFalse(StoodaProtocol.isValid("123 "))
        assertFalse(StoodaProtocol.isValid("-100"))
    }

    @Test
    fun testIsValidRejectsOutOfRangeStudentIndex() {
        // 64 is the first invalid student index
        // 6+4+0 = 10 -> checksum 0 -> code 6400
        assertFalse("Student index 64 should be rejected", StoodaProtocol.isValid("6400"))
        // 70 + type 0 + checksum 7 -> 7007
        assertFalse("Student index 70 should be rejected", StoodaProtocol.isValid("7007"))
        // 99 + type 0 + checksum 8 -> 9908
        assertFalse("Student index 99 should be rejected", StoodaProtocol.isValid("9908"))
    }

    @Test
    fun testIsValidRejectsOutOfRangeAnswerType() {
        // Types 6..9 should be rejected
        // Student 0, type 6, checksum 6 -> 0066
        assertFalse("Answer type 6 should be rejected", StoodaProtocol.isValid("0066"))
        assertFalse("Answer type 7 should be rejected", StoodaProtocol.isValid("0077"))
        assertFalse("Answer type 8 should be rejected", StoodaProtocol.isValid("0088"))
        assertFalse("Answer type 9 should be rejected", StoodaProtocol.isValid("0099"))
    }

    @Test
    fun testIsValidRejectsIncorrectChecksum() {
        // "0000" is valid (checksum 0). All other checksum digits should fail.
        for (wrongChecksum in 1..9) {
            val code = "000$wrongChecksum"
            assertFalse("Code with wrong checksum $code should be rejected", StoodaProtocol.isValid(code))
            assertNull("Parse should return null for $code", StoodaProtocol.parse(code))
        }
    }

    @Test
    fun testAnswerTypeToLetter() {
        assertEquals("A", StoodaProtocol.answerTypeToLetter(0))
        assertEquals("B", StoodaProtocol.answerTypeToLetter(1))
        assertEquals("C", StoodaProtocol.answerTypeToLetter(2))
        assertEquals("D", StoodaProtocol.answerTypeToLetter(3))
        assertEquals("E", StoodaProtocol.answerTypeToLetter(4))
        assertEquals("?", StoodaProtocol.answerTypeToLetter(5))
        assertEquals("6", StoodaProtocol.answerTypeToLetter(6))
        assertEquals("-1", StoodaProtocol.answerTypeToLetter(-1))
    }
}
