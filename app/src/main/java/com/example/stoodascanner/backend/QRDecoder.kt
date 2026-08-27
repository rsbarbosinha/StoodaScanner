package com.example.stoodascanner.backend

class QRDecoder(private val studentClass: StudentClass? = null) {

    /**
     * Decodes the raw 4-digit QR code string into a user-friendly format.
     *
     * Logic:
     * - First two digits (00-63): Index in the student list.
     * - Third digit (0-5): Mapped to letters A, B, C, D, E, or ?.
     * - Fourth digit: Checksum.
     */
    fun decode(qrCode: String, index: Int): String {
        val studentName = studentClass?.students?.getOrNull(index)
        val translatedId = if (studentName != null) {
            "${(index + 1).toString().padStart(2, '0')}: $studentName"
        } else {
            (index + 1).toString().padStart(2, '0')
        }

        if (qrCode.isEmpty()) return "$translatedId - (---)"
        if (qrCode.length < 3) return "$translatedId - $qrCode"

        return try {
            val thirdDigit = qrCode.substring(2, 3).toInt()

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
            "$translatedId - $qrCode"
        }
    }
}
