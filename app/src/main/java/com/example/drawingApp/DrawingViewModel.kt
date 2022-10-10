package com.example.drawingApp

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel

class DrawingViewModel : ViewModel() {

    data class ViewState(
        val color: Int,
        val layers: MutableList<String>,
        val activeBitmap: Bitmap?,
        val isAlertDialogOpen: Boolean,
        val layerDialogText: String,
        val isLayerSheetOpen: Boolean,
        val editIndex: Int?
    )

    @ColorInt
    var primaryColor = Color.BLACK

    private val layerNames: MutableList<String> = mutableListOf()

    var activeBitmap: Bitmap? = null

    var isLayerDialogOpen = false   //new dialog
    var currentLayerDialogText: String? = null

    var isViewingLayerSheetOpen = false       //edit layer

    //view inside edit layer
    var layerViewEditDialogIndex: Int? = null

    var onUpdate: ((ViewState) -> Unit)? = null

    private fun updateViewState() {
        onUpdate?.invoke(
            ViewState(
                primaryColor,
                layerNames,
                activeBitmap,
                isLayerDialogOpen,
                currentLayerDialogText ?: "",
                isViewingLayerSheetOpen,
                layerViewEditDialogIndex
            )
        )
    }

    fun initialize() {
        if (layerNames.isEmpty()) layerNames.add("Layer 1")
        updateViewState()
    }

    fun openAlertDialog() {
        isLayerDialogOpen = true
        updateViewState()
    }

    fun closeAlertDialog() {
        isLayerDialogOpen = false
        currentLayerDialogText = ""
        updateViewState()
    }

    fun openLayersSheet() {
        isViewingLayerSheetOpen = true
        updateViewState()
    }

    fun closeLayersSheet() {
        isViewingLayerSheetOpen = false
        layerViewEditDialogIndex = null
        updateViewState()
    }

    fun addLayer(layer: String) {
        layerNames.add(layer)
    }

    fun removeLayer(index: Int) {
        layerNames.removeAt(index)
    }

    fun replaceLayer(index: Int, replacementLayer: String) {
        layerNames[index] = replacementLayer
    }

}