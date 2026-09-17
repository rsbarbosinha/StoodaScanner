package com.example.stoodascanner.ui

import com.example.stoodascanner.data.StudentClass

object Mocks {
    val mockStudentClass = StudentClass(
        title = "Mathematics 101",
        nickname = "MAT",
        color = 0xFF4CAF50.toInt(),
        students = listOf("Alice", "Bob", "Charlie", "David", "Eve")
    )

    val mockClasses = listOf(
        mockStudentClass,
        StudentClass(
            title = "Physics Advanced",
            nickname = "PHY",
            color = 0xFF2196F3.toInt(),
            students = listOf("Frank", "Grace", "Heidi")
        ),
        StudentClass(
            title = "History of Art",
            nickname = "ART",
            color = 0xFFFF9800.toInt(),
            students = listOf("Ivan", "Judy", "Karl")
        )
    )

    val mockScannedCodes = listOf(
        "0000", // Alice - A
        "0112", // Bob - B
        "0224", // Charlie - C
        "",     // David - Missing
        "0404"  // Eve - A
    )
}
