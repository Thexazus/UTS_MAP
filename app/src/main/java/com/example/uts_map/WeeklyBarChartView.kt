package com.example.uts_map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class WeeklyBarChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val barPaint = Paint().apply {
        color = Color.parseColor("#40BFFF")
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }
    private var data: Map<String, Int> = emptyMap()

    fun setData(data: Map<String, Int>) {
        this.data = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val barWidth = width / (2 * data.size)
        var left = 0

        data.forEach { (day, value) ->
            val top = height - (value.toFloat() / 100) * (height - textPaint.textSize * 2)
            val right = left + barWidth
            val bottom = height.toFloat() - textPaint.textSize * 2

            // Draw bar
            canvas.drawRect(left.toFloat(), top, right.toFloat(), bottom, barPaint)

            // Draw day label
            canvas.drawText(day, left + barWidth / 2f, height.toFloat() - textPaint.textSize, textPaint)

            left += 2 * barWidth
        }

    }
}
