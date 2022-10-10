package com.example.drawingApp.CustomViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ClickableViewAccessibility")
class ColorSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    /**
     * called whenever the hue value is changed,
     * use to set hue value or to use the hue value that has been changed.
     */
    var onHueChange: ((Float) -> Unit)? = null

    /**
     * called on motionEvent Down
     * used to prevent dragging
     */
    var onDown: (() -> Unit)? = null

    /**
     * called on motionEvent cancel or up
     * used to allow dragging
     */
    var onUp: (() -> Unit)? = null

    var hue: Float = MIN_HUE

    private var held = false

    init {
        setOnTouchListener { _, motionEvent ->
            val hueCoordinate =
                max(min((MAX_HUE - (motionEvent.y / height) * MAX_HUE), MAX_HUE), MIN_HUE)
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    held = true
                    onDown?.invoke()
                    (this.parent as? ViewGroup)?.children?.forEach { it.clearFocus() }
                    onHueChange?.invoke(hueCoordinate)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    held = false
                    onUp?.invoke()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (held) {
                        onHueChange?.invoke(hueCoordinate)
                    }
                }
            }
            true
        }
    }

    fun sliderSetHue(newHue: Float) {
        hue = newHue
        invalidate()
    }

    private fun getBitmap() = sliderBitmap ?: generateBitmap().also { sliderBitmap = it }

    private fun generateBitmap(): Bitmap {
        var startHue = MAX_HUE
        val hStep = 1f / height * MAX_HUE
        val hsv = floatArrayOf(startHue, 1f, 1f)

        var index = 0
        val createdBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)

        repeat(height) {
            hsv[0] = startHue
            val color = Color.HSVToColor(hsv)
            repeat(width) {
                pixels[index] = color
                index += 1
            }
            startHue -= hStep
        }
        createdBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return createdBitmap
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { c ->
            getBitmap()?.let { c.drawBitmap(it, 0f, 0f, null) }
            val lineY = (MAX_HUE - min(hue, MAX_HUE)) / MAX_HUE * height
            c.drawLine(0f, lineY, width.toFloat(), lineY, selectionPaint)
        }
    }

    companion object {
        const val MIN_HUE = 0f
        const val MAX_HUE = 360f
        private var sliderBitmap: Bitmap? = null
        private val selectionPaint = Paint().apply {
            strokeWidth = 5f
            style = Paint.Style.STROKE
            color = Color.WHITE
        }
    }
}