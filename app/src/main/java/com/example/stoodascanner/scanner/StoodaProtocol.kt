package com.example.stoodascanner.scanner

data class ParsedQrCode(
    val studentIndex: Int,
    val answerType: Int,
    val answerLetter: String,
    val checksum: Int,
    val rawCode: String
)

object StoodaProtocol {
    const val MAX_STUDENTS = 64
    val ANSWER_LETTERS = listOf("A", "B", "C", "D", "E", "?")

    fun calculateChecksum(firstDigit: Int, secondDigit: Int, thirdDigit: Int): Int {
        return (firstDigit + secondDigit + thirdDigit) % 10
    }

    fun encode(studentIndex: Int, answerType: Int): String {
        require(studentIndex in 0 until MAX_STUDENTS) { "Student index must be between 0 and 63, got: $studentIndex" }
        require(answerType in 0..5) { "Answer type must be between 0 and 5, got: $answerType" }
        val firstDigit = studentIndex / 10
        val secondDigit = studentIndex % 10
        val thirdDigit = answerType
        val checksum = calculateChecksum(firstDigit, secondDigit, thirdDigit)
        return "$firstDigit$secondDigit$thirdDigit$checksum"
    }

    fun isValid(qrText: String): Boolean {
        if (!qrText.matches(Regex("^\\d{4}$"))) return false
        val firstDigit = qrText[0].digitToInt()
        val secondDigit = qrText[1].digitToInt()
        val thirdDigit = qrText[2].digitToInt()
        val forthDigit = qrText[3].digitToInt()
        val firstTwo = firstDigit * 10 + secondDigit

        return firstTwo in 0 until MAX_STUDENTS &&
                thirdDigit in 0..5 &&
                forthDigit == calculateChecksum(firstDigit, secondDigit, thirdDigit)
    }

    fun parse(qrText: String): ParsedQrCode? {
        if (!isValid(qrText)) return null
        val firstDigit = qrText[0].digitToInt()
        val secondDigit = qrText[1].digitToInt()
        val thirdDigit = qrText[2].digitToInt()
        val forthDigit = qrText[3].digitToInt()
        val studentIndex = firstDigit * 10 + secondDigit
        val letter = ANSWER_LETTERS.getOrElse(thirdDigit) { thirdDigit.toString() }
        return ParsedQrCode(
            studentIndex = studentIndex,
            answerType = thirdDigit,
            answerLetter = letter,
            checksum = forthDigit,
            rawCode = qrText
        )
    }

    fun answerTypeToLetter(type: Int): String {
        return ANSWER_LETTERS.getOrElse(type) { type.toString() }
    }
}
