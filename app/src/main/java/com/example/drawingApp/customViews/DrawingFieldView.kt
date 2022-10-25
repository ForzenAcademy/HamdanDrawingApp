package com.example.drawingApp.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    /**
     * Whenever a line is being drawn call this
     * specifically triggers on motion event move
     * Purpose is to update the model's bitmap whenever we are drawing on the bitmap
     * to keep the state of the bitmap consistent in the case of a outside interference
     * such as a phone call or a sudden orientation change
     */
    var onBitmapUpdate: ((Bitmap) -> Unit)? = null
    private var paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4f
        //avoid the issue of dealing handling possible uninitialized values, black is expected on starting a drawing app after all
    }
    private var canvasBitmap: Bitmap? = null
    private var heldDown = false    //used to track if the user is holding finger down on the view
    private var path = Path()
    private var drawingCanvas: Canvas? = null

    /**
     * used for setting our previous bitmap from rotation, no expectation of having drawn
     */
    fun setBitmap(bitmap: Bitmap) {
        canvasBitmap = bitmap
    }

    /**
     * sets the bitmap from some image.
     * If you want the image resized, pass true to resize and also a scope to launch
     * a coroutine to resize in, if either are not given in that situation it will proceed as usual. The defaults allow you to ignore that if resize is not occuring
     */
    fun setBitmapFromImageBitmap(
        imageBitmap: Bitmap,
        resize: Boolean = false,
        scope: CoroutineScope? = null
    ) {
        val finish = {
            canvasBitmap?.let { onBitmapUpdate?.invoke(it) }
            invalidate()
        }
        if (resize && scope != null) {
            scope.launch(Dispatchers.IO) {
                canvasBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, true)
                finish()
            }
        } else {
            canvasBitmap = imageBitmap
            finish()
        }
    }

    fun setPaintColor(paintColor: Int) {
        paint.color = paintColor
    }

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
                }
                MotionEvent.ACTION_MOVE -> {
                    //can add an if statement that if the motionEvent is out of bounds held = false
                    //depends if we want this behaviour
                    if (heldDown) {
                        path.lineTo(motionEvent.x, motionEvent.y)
                        drawingCanvas = canvasBitmap?.let { Canvas(it) }
                        canvasBitmap?.let { onBitmapUpdate?.invoke(it) }
                        drawingCanvas?.drawPath(path, paint)
                        invalidate()
                    }
                }
            }
            true
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { c ->
            if (canvasBitmap == null) {
                canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                canvasBitmap?.let { drawingCanvas = Canvas(it) }
                drawingCanvas?.drawColor(Color.LTGRAY)
            }
            canvasBitmap?.let { c.drawBitmap(it, 0f, 0f, null) }
        }
    }
}