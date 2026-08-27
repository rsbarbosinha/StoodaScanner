package com.example.stoodascanner

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

class ResultGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: Map<String, Int> = emptyMap()
    private val paintBar = Paint().apply { isAntiAlias = true }
    private val paintText = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private val paintLabel = Paint().apply {
        color = Color.DKGRAY
        textSize = 30f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val barColors = listOf(
        "#4CAF50".toColorInt(), // Green
        "#2196F3".toColorInt(), // Blue
        "#FFC107".toColorInt(), // Amber
        "#E91E63".toColorInt(), // Pink
        "#9C27B0".toColorInt(), // Purple
        "#FF5722".toColorInt()  // Deep Orange
    )

    fun setData(counts: Map<String, Int>) {
        this.data = counts.toSortedMap()
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val padding = 100f
        val graphWidth = width - 2 * padding
        val graphHeight = height - 2 * padding
        val maxCount = data.values.maxOrNull() ?: 1
        val barWidth = graphWidth / data.size * 0.8f
        val spacing = graphWidth / data.size * 0.2f

        var currentX = padding + spacing / 2

        data.entries.forEachIndexed { index, entry ->
            val barHeight = (entry.value.toFloat() / maxCount) * graphHeight
            
            // Draw Bar
            paintBar.color = barColors[index % barColors.size]
            canvas.drawRect(
                currentX,
                height - padding - barHeight,
                currentX + barWidth,
                height - padding,
                paintBar
            )

            // Draw Count Text
            canvas.drawText(
                entry.value.toString(),
                currentX + barWidth / 2,
                height - padding - barHeight - 20f,
                paintText
            )

            // Draw Label (A, B, C...)
            canvas.drawText(
                entry.key,
                currentX + barWidth / 2,
                height - padding + 50f,
                paintLabel
            )

            currentX += barWidth + spacing
        }

        // Draw baseline
        val paintLine = Paint().apply {
            color = Color.BLACK
            strokeWidth = 5f
        }
        canvas.drawLine(padding, height - padding, width - padding, height - padding, paintLine)
    }
}
