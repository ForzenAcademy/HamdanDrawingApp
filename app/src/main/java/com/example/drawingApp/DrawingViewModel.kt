package com.example.drawingApp

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawingApp.dataClasses.Hsv
import kotlinx.coroutines.CoroutineScope

class DrawingViewModel : ViewModel() {

    data class ViewState(
        val isColorSheetOpen: Boolean,
        val isLayerSheetOpen: Boolean,
        val isLayerDialogOpen: Boolean,
        val chosenColor: Int,
        val circleColor: Int,
        val layers: MutableList<String>,
        val layerDialogText: String,
        val activeBitmap: Bitmap?,
        val editIndex: Int?
    )

    private var isColorPickerSheetOpen = false

    private var chosenColor: Int? = null

    var activeBitmap: Bitmap? = null

    private var hsv = Hsv(0f, 0f, 0f)

    var circleColor: Int = Color.BLACK

    //used to help determine rgb values for consistency
    var activeColor: Int = Color.BLACK
    val layerNames: MutableList<String> = mutableListOf()

    fun openColorSheet() {
        isColorPickerSheetOpen = true
        updateViewState()
    }

    var isLayerDialogOpen = false   //new dialog
    var currentLayerDialogText: String? = null

    var isViewingLayerSheetOpen = false       //edit layer

    //view inside edit layer
    var layerViewEditDialogIndex: Int? = null

    var onUpdate: ((ViewState) -> Unit)? = null

    private fun updateViewState() {
        onUpdate?.invoke(
            ViewState(
                isColorSheetOpen = isColorPickerSheetOpen,
                isLayerSheetOpen = isViewingLayerSheetOpen,
                isLayerDialogOpen = isLayerDialogOpen,
                chosenColor = chosenColor ?: Color.BLACK,
                circleColor = circleColor,
                layers = layerNames,
                layerDialogText = currentLayerDialogText ?: "",
                activeBitmap = activeBitmap,
                editIndex = layerViewEditDialogIndex
            )
        )
    }

    fun closeColorSheet(isNotSubmitted: Boolean) {
        isColorPickerSheetOpen = false
        if (!isNotSubmitted) {
            hsv = Hsv(0f, 0f, 0f)
        }
        updateViewState()
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

    fun updateColorFromHsv() {
        hsv.toColor().let {
            activeColor = it
        }
    }

    fun setHsv(newHsv: Hsv) {
        hsv = newHsv
    }

    fun getHsv() = hsv

    fun setChosenColor(color: Int) {
        chosenColor = color
    }

    fun getViewModelScope(): CoroutineScope {
        return this.viewModelScope
    }

}