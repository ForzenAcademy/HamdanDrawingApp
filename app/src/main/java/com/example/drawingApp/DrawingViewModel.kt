package com.example.drawingApp

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel

class DrawingViewModel : ViewModel() {


    @ColorInt
    var primaryColor = Color.BLACK

    val layerNames: MutableList<String> = mutableListOf()

    var activeBitmap: Bitmap? = null

    var isLayerCreationDialogOpen = false
    var currentLayerCreationDialogText = ""


}