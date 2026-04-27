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
        color = Color.GREEN
        strokeWidth = 10f
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
        val size = 50f
        // Draw a simple checkmark
        canvas.drawLine(x - size / 2, y, x - size / 4, y + size / 2, paint)
        canvas.drawLine(x - size / 4, y + size / 2, x + size / 2, y - size / 2, paint)
    }
}
