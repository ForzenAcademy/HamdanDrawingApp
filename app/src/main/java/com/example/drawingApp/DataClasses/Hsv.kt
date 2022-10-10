package com.example.drawingApp.DataClasses

import android.graphics.Color

data class Hsv(
    val hue: Float,
    val saturation: Float,
    val value: Float,
) {
    fun toColor() = Color.HSVToColor(floatArrayOf(hue, saturation, value))

    companion object {

        fun fromColorToHsv(color: Int): Hsv {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            return Hsv(hsv[0], hsv[1], hsv[2])
        }

    }

}