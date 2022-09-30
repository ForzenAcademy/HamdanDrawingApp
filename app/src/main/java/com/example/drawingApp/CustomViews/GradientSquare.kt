package com.example.drawingApp.CustomViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ClickableViewAccessibility")
class GradientSquare @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var held = false
    private var drawRect: Rect? = null

    /**
     * called on saturation or value change.
     * use this to update any saturation of values held elsewhere.
     */
    lateinit var onSatOrValChange: (saturation: Float, value: Float) -> Unit

    /**
     * called when user presses down on the gradient,
     * used to prevent dragging
     */
    var onDown: (() -> Unit)? = null

    /**
     * called when user lifts off gradient or cancels,
     * allows the user to drag dialog again
     */
    var onUp: (() -> Unit)? = null
    var saturation = 1f
    var value = 1f
    var hue = 0f

    init {
        setOnTouchListener { _, motionEvent ->
            saturation = max(min(motionEvent.x / width, 1f), 0f)
            value = max(min((height - motionEvent.y) / height, 1f), 0f)
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    held = true
                    onDown?.invoke()
                    (this.parent as? ViewGroup)?.children?.forEach { it.clearFocus() }
                    onSatOrValChange(saturation, value)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    held = false
                    onUp?.invoke()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (held) {
                        onSatOrValChange(saturation, value)
                    }
                }
            }
            true
        }
    }

    private fun startCacheTask() {
        GlobalScope.launch(Dispatchers.IO) {
            regenerate()
            val startPoint = completedIndexes
            for (i in startPoint until MAX_BITMAP_COUNT) {
                regenerate()
                bitmaps?.set(i, generateBitmap(i))
                completedIndexes += 1
            }
            cachingState = cacheState.CACHED
        }
    }

    private fun getBitmap(): Bitmap {
        val rIndex = max(0, min(hue.toInt(), MAX_BITMAP_COUNT - 1))
        regenerate()
        return if (bitmaps?.get(rIndex) == null) {
            bitmaps?.set(rIndex, generateBitmap(hue.toInt()))
            bitmaps?.get(rIndex)!!
        } else bitmaps?.get(rIndex)!!
    }

    //needs the selectedHue as we are creating the array of bitmaps in the background by passing it the int
    private fun generateBitmap(selectedHue: Int): Bitmap {
        val hStep = 1f / imageSize
        val vStep = 1f / imageSize
        var v = 1f
        var s = 0f
        val hsv = floatArrayOf(selectedHue.toFloat(), 1f, 1f)

        var index = 0
        val createdBitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(imageSize * imageSize)

        repeat(imageSize) {
            repeat(imageSize) {
                hsv[1] = s
                hsv[2] = v
                pixels[index] = Color.HSVToColor(hsv)
                s += hStep
                index += 1
            }
            s = 0f
            v -= vStep
        }
        createdBitmap.setPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)
        return createdBitmap
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { c ->
            if (drawRect == null) drawRect = Rect(0, 0, width, height)
            synchronized(lock) {
                if (bitmaps == null) regenerate()
                if (cachingState == cacheState.NOT_CACHED) {
                    cachingState = cacheState.CACHING
                    startCacheTask()
                }
            }
            drawRect?.let { rect -> c.drawBitmap(getBitmap(), null, rect, null) }
            c.drawCircle(saturation * width, height - value * height, 5f, selectionPaint)
        }
    }

    enum class cacheState {
        NOT_CACHED,
        CACHING,
        CACHED
    }

    companion object {
        const val imageSize = 175
        const val MAX_BITMAP_COUNT = 360
        private var bitmaps: Array<Bitmap?>? = Array(MAX_BITMAP_COUNT) { null }
        private var completedIndexes: Int = 0
        private val lock: Any = Any()
        private val selectionPaint = Paint().apply {
            strokeWidth = 5f
            style = Paint.Style.STROKE
            color = Color.WHITE
        }
        var cachingState: cacheState = cacheState.NOT_CACHED

        /**
         * used in the case that the static values somehow gets deleted and set to null
         */
        fun regenerate() {
            if (bitmaps == null) {
                bitmaps = Array(MAX_BITMAP_COUNT) { null }
            }
        }

    }
}