package com.example.stoodascanner

class QRDecoder(private val studentClass: StudentClass? = null) {

    /**
     * Decodes the raw 4-digit QR code string into a user-friendly format.
     *
     * Logic:
     * - First two digits (00-63): Index in the student list.
     * - Third digit (0-5): Mapped to letters A, B, C, D, E, or ?.
     * - Fourth digit: Checksum.
     */
    fun decode(qrCode: String): String {
        if (qrCode.length < 3) return qrCode

        return try {
            val firstTwo = qrCode.substring(0, 2).toInt()
            val thirdDigit = qrCode.substring(2, 3).toInt()

            val studentName = studentClass?.students?.getOrNull(firstTwo)
            val translatedId = if (studentName != null) {
                "${(firstTwo + 1).toString().padStart(2, '0')}: $studentName"
            } else {
                (firstTwo + 1).toString().padStart(2, '0')
            }

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
        } catch (_: Exception) {
            qrCode
        }
    }
}
