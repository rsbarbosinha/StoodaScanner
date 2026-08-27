package com.example.stoodascanner.backend

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import zxingcpp.BarcodeReader
import zxingcpp.BarcodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class QRGenerator(private val context: Context) {

    // A4 size in points (72 points per inch)
    private val pageWidth = 595
    private val pageHeight = 842

    fun generateStoodaPdf(inputNames: List<String>) {
        Thread {
            try {
                val pdfDocument = PdfDocument()
                val writer = BarcodeWriter()

                for (id in inputNames.indices) {
                    val studentName = inputNames[id]
                    
                    // Generate all 6 QR codes for this student (A, B, C, D, E, ?)
                    val studentQrCodes = mutableListOf<String>()
                    for (type in 0..5) {
                        val firstDigit = id / 10
                        val secondDigit = id % 10
                        val thirdDigit = type
                        val checksum = (firstDigit + secondDigit + thirdDigit) % 10
                        studentQrCodes.add("$firstDigit$secondDigit$thirdDigit$checksum")
                    }

                    // Front Side (QR Codes)
                    val frontPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size).create()
                    val frontPage = pdfDocument.startPage(frontPageInfo)
                    drawFrontPage(frontPage.canvas, studentQrCodes, writer)
                    pdfDocument.finishPage(frontPage)

                    // Back Side (Numbers and Grid)
                    val backPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size).create()
                    val backPage = pdfDocument.startPage(backPageInfo)
                    drawBackPage(backPage.canvas, studentQrCodes, studentName)
                    pdfDocument.finishPage(backPage)
                }

                savePdf(pdfDocument)
            } catch (e: Exception) {
                e.printStackTrace()
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun drawFrontPage(canvas: Canvas, qrCodes: List<String>, writer: BarcodeWriter) {
        val cellWidth = pageWidth / 2f
        val cellHeight = pageHeight / 3f
        
        // Target size for QR code (70% of cell)
        val qrTargetSize = (cellWidth.coerceAtMost(cellHeight) * 0.7f).toInt()

        for (i in qrCodes.indices) {
            val col = i % 2
            val row = i / 2

            // Calculate exact center of the cell
            val centerX = col * cellWidth + cellWidth / 2f
            val centerY = row * cellHeight + cellHeight / 2f

            val bitmap = writer.encode(qrCodes[i], qrTargetSize, qrTargetSize, BarcodeReader.Format.MICRO_QR_CODE, "L", 0)
            
            val left = centerX - (bitmap.width / 2f)
            val top = centerY - (bitmap.height / 2f)
            
            canvas.drawBitmap(bitmap, left, top, null)
        }
    }

    private fun drawBackPage(canvas: Canvas, qrCodes: List<String>, studentName: String) {
        val cellWidth = pageWidth / 2f
        val cellHeight = pageHeight / 3f

        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val paintGrid = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // Draw outer and inner grid lines
        for (i in 0..2) {
            val x = i * cellWidth
            canvas.drawLine(x, 0f, x, pageHeight.toFloat(), paintGrid)
        }
        for (i in 0..3) {
            val y = i * cellHeight
            canvas.drawLine(0f, y, pageWidth.toFloat(), y, paintGrid)
        }

        val types = listOf("A", "B", "C", "D", "E", "?")

        for (i in qrCodes.indices) {
            // Mirror columns for back-side alignment
            val col = 1 - (i % 2)
            val row = i / 2

            val centerX = col * cellWidth + cellWidth / 2f
            val centerY = row * cellHeight + cellHeight / 2f
            val bottomY = (row + 1) * cellHeight - 20f // 20pt padding from the bottom of the cell

            // Draw student name and type
            canvas.drawText(studentName, centerX, centerY, paintText)
            canvas.drawText(types[i], centerX, bottomY, paintText)
        }
    }

    private fun savePdf(pdfDocument: PdfDocument) {
        val fileName = "StoodaQrTest.pdf"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                }
                val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                if (uri != null) {
                    outputStream = context.contentResolver.openOutputStream(uri)
                }
            } else {
                val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                if (!docsDir.exists()) docsDir.mkdirs()
                val file = File(docsDir, fileName)
                outputStream = FileOutputStream(file)
            }

            outputStream?.use {
                pdfDocument.writeTo(it)
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "PDF saved to Documents", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            (context as? Activity)?.runOnUiThread {
                Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            pdfDocument.close()
        }
    }
}
