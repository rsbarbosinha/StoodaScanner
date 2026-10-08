package com.example.stoodascanner.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

class ResultGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: Map<String, Int> = emptyMap()
    private var textColor: Int = Color.BLACK
    private var labelColor: Int = Color.DKGRAY
    private var baselineColor: Int = Color.BLACK

    private val paintBar = Paint().apply {
        isAntiAlias = true
    }
    private val barPath = Path()

    private val paintText = Paint().apply {
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private val paintLabel = Paint().apply {
        textSize = 30f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val barColors = listOf(
        "#FFADAD".toColorInt(), // Pastel Red
        "#FFD6A5".toColorInt(), // Pastel Orange
        "#FDFFB6".toColorInt(), // Pastel Yellow
        "#CAFFBF".toColorInt(), // Pastel Green
        "#9BF6FF".toColorInt(), // Pastel Cyan
        "#A0C4FF".toColorInt(), // Pastel Blue
        "#BDB2FF".toColorInt(), // Pastel Purple
        "#FFC6FF".toColorInt(), // Pastel Pink
        "#F0E6EF".toColorInt(), // Pastel Lilac
        "#E5E5E5".toColorInt(), // Pastel Greyish
        "#FFD1CC".toColorInt(), // Pastel Peach
        "#C1FBA4".toColorInt()  // Pastel Lime
    )

    fun setData(counts: Map<String, Int>, textColor: Int, labelColor: Int, baselineColor: Int) {
        this.data = counts.toSortedMap()
        this.textColor = textColor
        this.labelColor = labelColor
        this.baselineColor = baselineColor
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        paintText.color = textColor
        paintLabel.color = labelColor

        val padding = 100f
        val graphWidth = width - 2 * padding
        val graphHeight = height - 2 * padding
        val maxCount = data.values.maxOrNull() ?: 1
        val barWidth = graphWidth / data.size * 0.8f
        val spacing = graphWidth / data.size * 0.2f

        var currentX = padding + spacing / 2

        data.entries.forEachIndexed { index, entry ->
            val barHeight = (entry.value.toFloat() / maxCount) * graphHeight

            val left = currentX
            val top = height - padding - barHeight
            val right = currentX + barWidth
            val bottom = height - padding

            barPath.reset()
            // Dynamically round corners based on bar width, capping at 20f
            val cornerRadius = minOf(20f, barWidth / 4)
            val radii = floatArrayOf(
                cornerRadius, cornerRadius, // top-left
                cornerRadius, cornerRadius, // top-right
                0f, 0f,   // bottom-right
                0f, 0f    // bottom-left
            )
            
            // To prevent Path.addRoundRect issues when top >= bottom on zero-height bars
            val actualTop = if (bottom - top < 1f) bottom - 1f else top
            barPath.addRoundRect(left, actualTop, right, bottom, radii, Path.Direction.CW)

            // Draw Bar
            paintBar.color = barColors[index % barColors.size]
            canvas.drawPath(barPath, paintBar)

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
            color = baselineColor
            strokeWidth = 5f
        }
        canvas.drawLine(padding, height - padding, width - padding, height - padding, paintLine)
    }
}