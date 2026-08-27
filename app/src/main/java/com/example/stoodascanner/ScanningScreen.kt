package com.example.stoodascanner

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

@Composable
fun ScanningScreen(
    scannedCount: Int,
    targetCount: Int,
    isDebugMode: Boolean,
    analysisResolution: String,
    missingStudents: List<String> = emptyList(),
    onFinish: () -> Unit,
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

        // Missing students overlay
        if (missingStudents.isNotEmpty() && missingStudents.size <= 3) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.awaiting),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    missingStudents.forEach { name ->
                        Text(
                            text = name,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)).padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.scanned_format, scannedCount, targetCount),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        // Finish button if some codes were scanned but not all
        if (scannedCount > 0 && scannedCount < targetCount) {
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.finish_scanning),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isDebugMode) {
            Box(
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)).padding(8.dp)
            ) {
                Text(
                    text = analysisResolution.ifEmpty { stringResource(R.string.initializing) },
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
