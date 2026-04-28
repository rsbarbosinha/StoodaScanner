package com.example.stoodascanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ScanOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val points = mutableListOf<TimedPoint>()
    private val paint = Paint().apply {
        color = Color.argb(128, 0, 255, 0) // Semi-transparent green
        strokeWidth = 6f // Thinner lines for a smaller checkmark
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private data class TimedPoint(val x: Float, val y: Float, val timestamp: Long)

    fun addPoint(x: Float, y: Float) {
        synchronized(points) {
            points.add(TimedPoint(x, y, System.currentTimeMillis()))
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentTime = System.currentTimeMillis()
        
        synchronized(points) {
            val iterator = points.iterator()
            while (iterator.hasNext()) {
                val point = iterator.next()
                if (currentTime - point.timestamp > 200) {
                    iterator.remove()
                } else {
                    drawCheckmark(canvas, point.x, point.y)
                }
            }
        }

        if (points.isNotEmpty()) {
            postInvalidateDelayed(16) // Aim for ~60fps if there are active points
        }
    }

    private fun drawCheckmark(canvas: Canvas, x: Float, y: Float) {
        val checkSize = 25f
        val circleRadius = 30f

        // Draw the circle around the point
        canvas.drawCircle(x, y, circleRadius, paint)

        // Draw the smaller checkmark inside
        // Adjusted coordinates to keep it centered within the circle
        canvas.drawLine(x - checkSize / 2, y, x - checkSize / 6, y + checkSize / 3, paint)
        canvas.drawLine(x - checkSize / 6, y + checkSize / 3, x + checkSize / 2, y - checkSize / 3, paint)
    }
}
