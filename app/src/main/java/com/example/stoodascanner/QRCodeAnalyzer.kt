package com.example.stoodascanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

class QRCodeAnalyzer(private val onQrCodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()

    override fun analyze(image: ImageProxy) {
        // CameraX produces YUV_420_888 format by default
        val yBuffer = image.planes[0].buffer
        val ySize = yBuffer.remaining()
        val yArray = ByteArray(ySize)
        yBuffer.get(yArray)

        val source = PlanarYUVLuminanceSource(
            yArray, image.width, image.height,
            0, 0, image.width, image.height, false
        )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decode(binaryBitmap)
            onQrCodeScanned(result.text)
        } catch (e: Exception) {
            // ZXing throws an exception if no code is found. We just catch and ignore it.
        } finally {
            image.close() // CRITICAL: Always close the image to receive the next one!
        }
    }
}