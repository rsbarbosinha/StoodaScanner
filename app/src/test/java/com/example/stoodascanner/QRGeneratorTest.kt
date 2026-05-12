package com.example.stoodascanner

import org.junit.Assert.assertEquals
import org.junit.Test

class QRGeneratorTest {

    @Test
    fun testQrCodeFormula() {
        val studentIndex = 12
        val types = 0..5
        val expected = listOf("1203", "1214", "1225", "1236", "1247", "1258")
        
        val actual = mutableListOf<String>()
        for (type in types) {
            val firstDigit = studentIndex / 10
            val secondDigit = studentIndex % 10
            val thirdDigit = type
            val checksum = (firstDigit + secondDigit + thirdDigit) % 10
            actual.add("$firstDigit$secondDigit$thirdDigit$checksum")
        }
        
        assertEquals(expected, actual)
    }

    @Test
    fun testQrCodeFormulaEdgeCase() {
        val studentIndex = 63
        val type = 5
        val firstDigit = studentIndex / 10
        val secondDigit = studentIndex % 10
        val thirdDigit = type
        val checksum = (firstDigit + secondDigit + thirdDigit) % 10
        val qr = "$firstDigit$secondDigit$thirdDigit$checksum"
        
        assertEquals("6354", qr) // (6+3+5)%10 = 14%10 = 4
    }
}
