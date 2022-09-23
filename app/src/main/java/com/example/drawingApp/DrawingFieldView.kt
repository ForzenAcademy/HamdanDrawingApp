package com.example.drawingApp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


@SuppressLint("ClickableViewAccessibility")
/**
 * Creates a Field to draw in with a color
 * use update color to change the stroke color
 * uses an internal canvas to draw on a saved bitmap member variable
 */
class DrawingFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var onFinishLine: ((Bitmap) -> Unit)? = null
    private var paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4f
        //avoid the issue of dealing handling possible uninitialized values, black is expected on starting a drawing app after all
    }
    private var canvasBitmap: Bitmap? = null
    private var heldDown = false    //used to track if the user is holding finger down on the view
    private var path = Path()
    private var calcCanvas: Canvas? = null


    init {
        setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    heldDown = true
                    path.moveTo(motionEvent.x, motionEvent.y)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    heldDown = false
                    path = Path()
                    canvasBitmap?.let { onFinishLine?.invoke(it) }
                }
                MotionEvent.ACTION_MOVE -> {
                    //can add an if statement that if the motionEvent is out of bounds held = false
                    //depends if we want this behaviour
                    if (heldDown) {
                        path.lineTo(motionEvent.x, motionEvent.y)
                        calcCanvas = canvasBitmap?.let { Canvas(it) }
                        calcCanvas?.drawPath(path, paint)
                        invalidate()
                    }
                }
            }
            true
        }
    }

    fun updateColor(c: Int) {
        paint.color = c
    }

    /**
     * To set the active drawing bitmap, This is for when the viewmodel needs to
     * load in the bitmap to have continuity. Will also be useful if other bitmaps need
     * to be drawn on.
     */
    fun setBitmap(b: Bitmap) {
        canvasBitmap = b
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let { c ->
            if (canvasBitmap == null) {
                canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                canvasBitmap?.let { calcCanvas = Canvas(it) }
                calcCanvas?.drawColor(Color.LTGRAY)
            }
            canvasBitmap?.let { c.drawBitmap(it, 0f, 0f, null) }
        }
    }
}