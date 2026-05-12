package com.example.stoodascanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import zxingcpp.BarcodeReader

class QRCodeAnalyzer(
    private val maxCodes: Int,
    private val onResolutionUpdate: (Int, Int, Int, Double) -> Unit,
    private val onQrCodeScanned: (String, Int, Int, Int, Int) -> Unit
) : ImageAnalysis.Analyzer {
    
    private var lastResolutionUpdate = 0L
    private var frameCount = 0
    private var lastFpsTime = 0L

    // Initialize the C++ reader with desired options
    private val reader = BarcodeReader(BarcodeReader.Options().apply {
        tryHarder = true
        maxNumberOfSymbols = maxCodes
    })

    override fun analyze(image: ImageProxy) {
        try {
            frameCount++
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastResolutionUpdate > 1000) {
                val fps = if (lastFpsTime > 0) {
                    frameCount * 1000.0 / (currentTime - lastFpsTime)
                } else 0.0
                
                onResolutionUpdate(image.width, image.height, image.imageInfo.rotationDegrees, fps)
                
                lastResolutionUpdate = currentTime
                frameCount = 0
                lastFpsTime = currentTime
            }

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