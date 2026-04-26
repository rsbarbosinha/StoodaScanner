package com.example.stoodascanner

import java.util.Locale

class QRDecoder {

    /**
     * Decodes the raw 4-digit QR code string into a user-friendly format.
     * 
     * Logic:
     * - First two digits (00-63): Incremented by 1 (01-64).
     * - Third digit (0-5): Mapped to letters A, B, C, D, E, or ?.
     * - Fourth digit: Checksum (ignored for display).
     */
    fun decode(qrCode: String): String {
        if (qrCode.length < 3) return qrCode

        return try {
            val firstTwo = qrCode.substring(0, 2).toInt()
            val thirdDigit = qrCode.substring(2, 3).toInt()

            val translatedId = String.format(Locale.US, "%02d", firstTwo + 1)
            val translatedType = when (thirdDigit) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                3 -> "D"
                4 -> "E"
                5 -> "?"
                else -> thirdDigit.toString()
            }

            "$translatedId - $translatedType"
        } catch (e: Exception) {
            qrCode
        }
    }
}
