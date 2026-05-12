package com.example.stoodascanner

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

@Composable
fun ScanningScreen(
    scannedCount: Int,
    targetCount: Int,
    onStartCamera: (PreviewView, (Float, Float) -> Unit) -> CameraManager
) {
    val overlayPoints = remember { mutableStateListOf<TimedPoint>() }
    var cameraManager: CameraManager? by remember { androidx.compose.runtime.mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                if (cameraManager == null) {
                    cameraManager = onStartCamera(previewView) { x, y ->
                        overlayPoints.add(TimedPoint(x, y, System.currentTimeMillis()))
                    }
                }
            }
        )

        DisposableEffect(Unit) {
            onDispose {
                cameraManager?.shutdown()
            }
        }

        // Canvas for checkmarks
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currentTime = System.currentTimeMillis()
            val iterator = overlayPoints.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                if (currentTime - p.timestamp > 200) {
                    iterator.remove()
                } else {
                    val paintColor = Color(0x8000FF00)
                    drawCircle(color = paintColor, radius = 30f, center = Offset(p.x, p.y), style = Stroke(width = 6f))
                    
                    val checkSize = 25f
                    drawLine(
                        color = paintColor,
                        start = Offset(p.x - checkSize / 2, p.y),
                        end = Offset(p.x - checkSize / 6, p.y + checkSize / 3),
                        strokeWidth = 6f
                    )
                    drawLine(
                        color = paintColor,
                        start = Offset(p.x - checkSize / 6, p.y + checkSize / 3),
                        end = Offset(p.x + checkSize / 2, p.y - checkSize / 3),
                        strokeWidth = 6f
                    )
                }
            }
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
                .background(Color.Black.copy(alpha = 0.5f)).padding(12.dp)
        ) {
            Text(
                text = "Scanned: $scannedCount / $targetCount",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }

    // Re-draw loop for animation
    LaunchedEffect(overlayPoints.size) {
        while (overlayPoints.isNotEmpty()) {
            delay(16)
        }
    }
}

data class TimedPoint(val x: Float, val y: Float, val timestamp: Long)
