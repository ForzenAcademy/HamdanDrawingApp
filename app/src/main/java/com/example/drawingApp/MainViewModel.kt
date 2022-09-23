package com.example.drawingApp

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {


    @ColorInt
    var primaryColor = Color.BLACK

    val layers: MutableList<String> = mutableListOf()
    var layerCounter = 1

    var activeBitmap: Bitmap? = null

    var dialogOpen = false
    var dialogText = ""


}