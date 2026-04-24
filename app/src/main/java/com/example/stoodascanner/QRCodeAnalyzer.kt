package com.example.stoodascanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import zxingcpp.BarcodeReader

class QRCodeAnalyzer(
    private val maxCodes: Int,
    private val onQrCodeScanned: (String, Int, Int, Int, Int) -> Unit // passing text + imageWidth + imageHeight + rawX + rawY
) : ImageAnalysis.Analyzer {

    // Initialize the C++ reader with desired options
    private val reader = BarcodeReader(BarcodeReader.Options().apply {
        tryHarder = true
        // CRITICAL: Tell ZXing to keep searching until it finds this many symbols
        maxNumberOfSymbols = maxCodes
        // Optional: formats = setOf(BarcodeReader.Format.QR_CODE)
    })

    override fun analyze(image: ImageProxy) {
        try {
            val results = reader.read(image)

            if (results.isNotEmpty()) {
                // CRITICAL: Loop through ALL found barcodes instead of taking just the first one
                for (result in results) {
                    val resultText = result.text
                    if (!resultText.isNullOrEmpty()) {
                        val centerX = result.position.let {
                            (it.topLeft.x + it.topRight.x + it.bottomLeft.x + it.bottomRight.x) / 4
                        }
                        val centerY = result.position.let {
                            (it.topLeft.y + it.topRight.y + it.bottomLeft.y + it.bottomRight.y) / 4
                        }
                        onQrCodeScanned(resultText, image.width, image.height, centerX, centerY)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            image.close()
        }
    }
}