package com.example.stoodascanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import zxingcpp.BarcodeReader

class QRCodeAnalyzer(private val onQrCodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {

    // Initialize the C++ reader with desired options
    private val reader = BarcodeReader(BarcodeReader.Options().apply {
        // tryHarder is slower but more accurate
        tryHarder = true
        // Optional: specify formats if you only need QR codes
        // formats = setOf(BarcodeReader.Format.QR_CODE)
    })

    override fun analyze(image: ImageProxy) {
        try {
            /*
             * ZXing-cpp provides a 'read(image: ImageProxy)' extension.
             * This replaces the need for PlanarYUVLuminanceSource and HybridBinarizer.
             */
            val results = reader.read(image)

            if (results.isNotEmpty()) {
                // Get the text from the first barcode found in the frame
                val resultText = results[0].text
                if (!resultText.isNullOrEmpty()) {
                    onQrCodeScanned(resultText)
                }
            }
        } catch (e: Exception) {
            // Log error or ignore
            e.printStackTrace()
        } finally {
            // CRITICAL: Always close the image to avoid blocking the CameraX pipeline
            image.close()
        }
    }
}