package com.example.uts_map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CalendarHeatmapView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint()

    private var data: Map<Int, Int> = emptyMap() // Map of day to value

    fun setData(data: Map<Int, Int>) {
        this.data = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cellSize = width / 7 // Assuming 7 columns for days of the week
        val cellPadding = 10

        data.forEach { (day, value) ->
            val row = (day - 1) / 7
            val col = (day - 1) % 7

            paint.color = getColorForValue(value)

            val left = col * cellSize + cellPadding
            val top = row * cellSize + cellPadding
            val right = left + cellSize - cellPadding
            val bottom = top + cellSize - cellPadding

            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }

    private fun getColorForValue(value: Int): Int {
        return when {
            value > 75 -> Color.RED
            value > 50 -> Color.YELLOW
            value > 25 -> Color.GREEN
            else -> Color.BLUE
        }
    }
}
