package com.example.stoodascanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.example.stoodascanner.data.StudentClass
import com.example.stoodascanner.ui.theme.StoodaScannerTheme

private val SquircleShape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radius = size.minDimension / 2f
        val polygon = RoundedPolygon(
            numVertices = 4,
            radius = radius,
            centerX = size.width / 2f,
            centerY = size.height / 2f,
            rounding = CornerRounding(
                radius = radius * 0.3f,
                smoothing = 1f
            )
        )
        val path = polygon.toPath().asComposePath()
        val matrix = androidx.compose.ui.graphics.Matrix()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        matrix.translate(centerX, centerY)
        matrix.rotateZ(45f)
        matrix.translate(-centerX, -centerY)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@Composable
fun FloatingSessionBubble(
    studentClass: StudentClass,
    missingCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val title = studentClass.title ?: ""
    val nickname = (studentClass.nickname ?: "").ifEmpty { 
        if (title.length >= 3) {
            title.substring(0, 3)
        } else if (title.length >= 2) {
            title.substring(0, 2)
        } else {
            title
        }
    }.uppercase()

    val bubbleColor = if (studentClass.color != null) Color(studentClass.color) else MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        // Main Bubble Box
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(SquircleShape)
                .background(bubbleColor)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nickname,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Badge - Positioned to pop out/overflow the bubble
        if (missingCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 0.dp, y = (-2).dp)
                    .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F))
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = missingCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun FloatingSessionBubblePreview() {
    StoodaScannerTheme {
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            FloatingSessionBubble(
                studentClass = StudentClass(
                    title = "Morning Class",
                    nickname = "AMC",
                    color = 0xFFEF8F4D.toInt()
                ),
                missingCount = 12,
                modifier = Modifier.padding(16.dp),
                onClick = {}
            )
        }
    }
}
