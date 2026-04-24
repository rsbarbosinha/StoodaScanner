package com.example.stoodascanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class QrCodeOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Simple data class to hold the mapped screen coordinates
    data class Point(val x: Float, val y: Float)

    private val scannedPoints = mutableListOf<Point>()

    // Paint for the checkmark line
    private val checkmarkPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    // Paint for the translucent background circle
    private val circlePaint = Paint().apply {
        color = Color.parseColor("#4000FF00") // Semi-transparent green
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun addPoint(x: Float, y: Float) {
        scannedPoints.add(Point(x, y))
        invalidate() // Tells Android to trigger onDraw() again
    }

    fun clear() {
        scannedPoints.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (point in scannedPoints) {
            val cx = point.x
            val cy = point.y
            val radius = 45f

            // 1. Draw the background circle
            canvas.drawCircle(cx, cy, radius, circlePaint)

            // 2. Draw the checkmark
            val path = Path()
            path.moveTo(cx - 15f, cy)            // Start left
            path.lineTo(cx - 5f, cy + 15f)       // Down to the dip
            path.lineTo(cx + 20f, cy - 15f)      // Up to the right

            canvas.drawPath(path, checkmarkPaint)
        }
    }
}