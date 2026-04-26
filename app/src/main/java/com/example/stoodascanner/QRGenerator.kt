package com.example.stoodascanner

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
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

    fun generateStoodaPdf(inputNumbers: List<String> = listOf("0000", "0112", "0224", "0336", "0448", "0550", "0606", "0718", "0820", "0932", "1045", "1157", "1203", "1315", "1427")) {
        Thread {
            try {
                val pdfDocument = PdfDocument()
                val writer = BarcodeWriter()

                val chunks = inputNumbers.chunked(6)

                for (chunk in chunks) {
                    // Front Side (QR Codes)
                    val frontPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size).create()
                    val frontPage = pdfDocument.startPage(frontPageInfo)
                    drawFrontPage(frontPage.canvas, chunk, writer)
                    pdfDocument.finishPage(frontPage)

                    // Back Side (Numbers and Grid)
                    val backPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size).create()
                    val backPage = pdfDocument.startPage(backPageInfo)
                    drawBackPage(backPage.canvas, chunk)
                    pdfDocument.finishPage(backPage)
                }

                savePdf(pdfDocument)
            } catch (e: Exception) {
                e.printStackTrace()
                (context as? android.app.Activity)?.runOnUiThread {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun drawFrontPage(canvas: Canvas, numbers: List<String>, writer: BarcodeWriter) {
        val cellWidth = pageWidth / 2f
        val cellHeight = pageHeight / 3f
        
        // Target size for QR code (70% of cell)
        val qrTargetSize = (cellWidth.coerceAtMost(cellHeight) * 0.7f).toInt()

        for (i in numbers.indices) {
            val col = i % 2
            val row = i / 2

            // Calculate exact center of the cell
            val centerX = col * cellWidth + cellWidth / 2f
            val centerY = row * cellHeight + cellHeight / 2f

            val bitmap = writer.encode(numbers[i], qrTargetSize, qrTargetSize, BarcodeReader.Format.MICRO_QR_CODE, "L", 0)
            
            // Draw the bitmap centered on the cell center
            // Subtracting HALF of the ACTUAL bitmap width/height from the cell center
            val left = centerX - (bitmap.width / 2f)
            val top = centerY - (bitmap.height / 2f)
            
            canvas.drawBitmap(bitmap, left, top, null)
        }
    }

    private fun drawBackPage(canvas: Canvas, numbers: List<String>) {
        val cellWidth = pageWidth / 2f
        val cellHeight = pageHeight / 3f
        val decoder = QRDecoder()

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

        for (i in numbers.indices) {
            // Mirror columns for back-side alignment
            val col = 1 - (i % 2)
            val row = i / 2

            val centerX = col * cellWidth + cellWidth / 2f
            val bottomY = (row + 1) * cellHeight - 20f // 20pt padding from the bottom of the cell

            val decodedText = decoder.decode(numbers[i])
            canvas.drawText(decodedText, centerX, bottomY, paintText)
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
                (context as? android.app.Activity)?.runOnUiThread {
                    Toast.makeText(context, "PDF saved to Documents", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            (context as? android.app.Activity)?.runOnUiThread {
                Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            pdfDocument.close()
        }
    }
}
