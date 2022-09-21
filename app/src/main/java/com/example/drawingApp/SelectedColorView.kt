package com.example.drawingApp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


@SuppressLint("ClickableViewAccessibility")
/**
 * This class is ued to create a custom view
 * the view shows a solid circle
 * when clicked it will cycle forward in its color order
 * the color order is Black, Red, Green, Blue
 *
 */
class SelectedColorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var states = arrayOf(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE)
    private var state = states[0]
    private val paint = Paint()

    /**
     * pass the selected color and assign it to given variables
     * use getState function from the SelectedColorView object to get the color
     * will be called everytime the color is changed
     */
    var onColorChange: ((Int) -> Unit)? = null


    init {
        setOnTouchListener { _, mE ->
            when (mE.action) {
                MotionEvent.ACTION_DOWN -> {
                    cycleColor()
                    onColorChange?.invoke(state)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    invalidate()
                }
            }
            true
        }
    }

    //cycles state of the state variable as well as changing color of the circles paint
    private fun cycleColor() {
        val index = states.indexOf(state)
        state = if (index == states.size - 1) states[0] else states[index + 1]
        paint.color = state
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            val half = width.toFloat() / 2f
            drawCircle(half, half, half, paint)
        }
    }
}