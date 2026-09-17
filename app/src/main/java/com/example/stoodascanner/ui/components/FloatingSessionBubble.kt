package com.example.stoodascanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stoodascanner.data.StudentClass

@Composable
fun FloatingSessionBubble(
    studentClass: StudentClass,
    missingCount: Int,
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
        modifier = Modifier
            .padding(16.dp, 16.dp, 20.dp, 20.dp)
            .size(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bubbleColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nickname,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (missingCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFD32F2F))
                    .padding(horizontal = 5.dp),
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
