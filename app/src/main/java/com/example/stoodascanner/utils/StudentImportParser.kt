package com.example.stoodascanner.utils

import android.content.Context
import android.net.Uri
import android.util.Xml
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipInputStream

object StudentImportParser {

    fun parseFileToNames(uri: Uri, context: Context, targetColIndex: Int): List<String> {
        val names = mutableListOf<String>()
        var headerChecked = false
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            
            if (mimeType == "text/comma-separated-values" || mimeType == "text/csv" || uri.path?.endsWith(".csv") == true) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    csvReader().open(inputStream) {
                        readAllAsSequence().forEach { row ->
                            if (names.size >= 64) return@forEach
                            if (targetColIndex >= 0 && targetColIndex < row.size) {
                                val value = row[targetColIndex].trim()
                                if (value.isNotEmpty()) {
                                    if (!headerChecked) {
                                        headerChecked = true
                                        if (value.contains(" ")) {
                                            names.add(value)
                                        }
                                    } else {
                                        names.add(value)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val sharedStrings = mutableListOf<String>()
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val zipIn = ZipInputStream(input)
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        if (entry.name == "xl/sharedStrings.xml") {
                            parseSharedStrings(zipIn, sharedStrings)
                            break
                        }
                        entry = zipIn.nextEntry
                    }
                }

                context.contentResolver.openInputStream(uri)?.use { input ->
                    val zipIn = ZipInputStream(input)
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        if (entry.name == "xl/worksheets/sheet1.xml") {
                            parseSheet(zipIn, sharedStrings, names, targetColIndex)
                            break
                        }
                        entry = zipIn.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            // In a real app, you'd propagate this error to the UI via a State or Result
            e.printStackTrace()
        }
        return names.take(64)
    }

    private fun parseSharedStrings(inputStream: InputStream, sharedStrings: MutableList<String>) {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var currentString = StringBuilder()
        var insideT = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "si") {
                        currentString = StringBuilder()
                    } else if (parser.name == "t") {
                        insideT = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideT) {
                        currentString.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") {
                        insideT = false
                    } else if (parser.name == "si") {
                        sharedStrings.add(currentString.toString())
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseSheet(inputStream: InputStream, sharedStrings: List<String>, names: MutableList<String>, targetColIdx: Int) {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var isSharedString = false
        var currentCellValue = ""
        var currentCellRef: String? = null
        val rowCells = mutableMapOf<String, String>()
        val targetColumn = indexToColLetter(targetColIdx)
        var headerChecked = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> {
                            rowCells.clear()
                        }
                        "c" -> {
                            currentCellRef = parser.getAttributeValue(null, "r")
                            val type = parser.getAttributeValue(null, "t")
                            isSharedString = (type == "s")
                        }
                        "v" -> {
                            currentCellValue = ""
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    currentCellValue += parser.text
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "v") {
                        val value = if (isSharedString) {
                            val index = currentCellValue.toIntOrNull()
                            if (index != null && index in sharedStrings.indices) sharedStrings[index] else ""
                        } else {
                            currentCellValue
                        }
                        val col = currentCellRef?.filter { it.isLetter() } ?: ""
                        if (col.isNotEmpty()) {
                            rowCells[col] = value
                        }
                    } else if (parser.name == "row") {
                        val value = rowCells[targetColumn]
                        if (value != null && value.trim().isNotEmpty()) {
                            val trimmedValue = value.trim()
                            if (!headerChecked) {
                                headerChecked = true
                                if (trimmedValue.contains(" ")) {
                                    names.add(trimmedValue)
                                }
                            } else {
                                names.add(trimmedValue)
                            }
                        }
                    }
                }
            }
            if (names.size >= 64) break
            eventType = parser.next()
        }
    }

    fun indexToColLetter(index: Int): String {
        var n = index
        val result = StringBuilder()
        while (n >= 0) {
            result.insert(0, ('A'.code + (n % 26)).toChar())
            n = (n / 26) - 1
        }
        return result.toString()
    }
}
