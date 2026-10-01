package com.example.stoodascanner

import com.example.stoodascanner.data.StudentClass
import com.example.stoodascanner.scanner.QRDecoder
import org.junit.Assert.assertEquals
import org.junit.Test

class QRDecoderTest {

    @Test
    fun testDecodeWithoutStudentClass() {
        val decoder = QRDecoder()

        assertEquals("01 - A", decoder.decode("0000", 0))
        assertEquals("01 - B", decoder.decode("0011", 0))
        assertEquals("01 - C", decoder.decode("0022", 0))
        assertEquals("01 - D", decoder.decode("0033", 0))
        assertEquals("01 - E", decoder.decode("0044", 0))
        assertEquals("01 - ?", decoder.decode("0055", 0))

        // Index padding
        assertEquals("10 - A", decoder.decode("0909", 9))
        assertEquals("64 - A", decoder.decode("6309", 63))
    }

    @Test
    fun testDecodeWithEmptyOrShortCode() {
        val decoder = QRDecoder()

        assertEquals("01 - (---)", decoder.decode("", 0))
        assertEquals("01 - 1", decoder.decode("1", 0))
        assertEquals("01 - 12", decoder.decode("12", 0))
    }

    @Test
    fun testDecodeWithStudentClass() {
        val studentClass = StudentClass(
            title = "Math 101",
            students = listOf("Alice Smith", "Bob Jones", "Charlie Brown")
        )
        val decoder = QRDecoder(studentClass)

        assertEquals("01: Alice Smith - A", decoder.decode("0000", 0))
        assertEquals("02: Bob Jones - B", decoder.decode("0112", 1))
        assertEquals("03: Charlie Brown - ?", decoder.decode("0257", 2))

        // Index beyond student list falls back to number only
        assertEquals("04 - A", decoder.decode("0303", 3))

        // Empty code with student name
        assertEquals("01: Alice Smith - (---)", decoder.decode("", 0))
        assertEquals("02: Bob Jones - 9", decoder.decode("9", 1))
    }

    @Test
    fun testDecodeWithNonNumericThirdChar() {
        val decoder = QRDecoder()

        // "00X0" has 'X' as 3rd char (index 2) -> substring(2, 3).toInt() fails -> returns "$translatedId - $qrCode"
        assertEquals("01 - 00X0", decoder.decode("00X0", 0))
    }
}
