package com.example.stoodascanner.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class StudentClass(
    val title: String,
    val students: List<String> // Index corresponds to QR ID (0-63)
)

class ClassManager(private val context: Context) {
    private val gson = Gson()
    private val classesFile = File(context.filesDir, "classes.json")

    fun saveClass(studentClass: StudentClass) {
        val classes = getAllClasses().toMutableList()
        // Remove existing class with same title if any
        classes.removeAll { it.title == studentClass.title }
        classes.add(studentClass)
        classesFile.writeText(gson.toJson(classes))
    }

    fun getAllClasses(): List<StudentClass> {
        if (!classesFile.exists()) return emptyList()
        val type = object : com.google.gson.reflect.TypeToken<List<StudentClass>>() {}.type
        return try {
            gson.fromJson(classesFile.readText(), type)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getClass(title: String): StudentClass? {
        return getAllClasses().find { it.title == title }
    }

    fun deleteClass(title: String) {
        val classes = getAllClasses().toMutableList()
        classes.removeAll { it.title == title }
        classesFile.writeText(gson.toJson(classes))
    }
}
