package com.example.drawingApp

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawingApp.dataClasses.Hsv
import com.example.drawingApp.utils.DialogUtility
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope

class DrawingViewModel(handle: SavedStateHandle) : ViewModel() {

    data class ViewState(
        val isColorSheetOpen: Boolean,
        val isLayerSheetOpen: Boolean,
        val isLayerDialogOpen: Boolean,
        val isDeleteDialogOpen: Boolean,
        val tabSheetState: DialogUtility.TabState,
        val chosenColor: Int,
        val circleColor: Int,
        val layers: MutableList<String>,
        val layerDialogText: String,
        val activeBitmap: Bitmap?,
        val editIndex: Int?
    )

    private val layerNames: MutableList<String> = mutableListOf()

    private var isColorPickerSheetOpen = false
    private var chosenColor: Int = Color.BLACK
    private var isLayerDialogOpen = false   //new dialog
    private var isDeleteDialogOpen = false //new delete dialog
    private var isViewingLayerSheetOpen = false       //edit layer
    private var _hsv = Hsv(0f, 0f, 0f)
    private var tabSheetState = DialogUtility.TabState(
        BottomSheetBehavior.STATE_COLLAPSED,
        DialogUtility.ToolEnum.Gradient,
        false
    )

    //used to help determine rgb values for consistency
    private var activeColor: Int = Color.BLACK
    private var _currentLayerDialogText: String? = null

    //view inside edit layer
    private var _layerViewEditDialogIndex: Int? = null

    var activeBitmap: Bitmap? = null
    var circleColor: Int = Color.BLACK

    /**
     * used to determine what happens whenever the view is updated / needed to be updated
     * This is where you will be opening dialogs and things based on whether they were open
     * or need to be open.
     */
    var onUpdate: ((ViewState) -> Unit)? = null

    private fun updateViewState() {
        onUpdate?.invoke(
            ViewState(
                isColorSheetOpen = isColorPickerSheetOpen,
                isLayerSheetOpen = isViewingLayerSheetOpen,
                isLayerDialogOpen = isLayerDialogOpen,
                isDeleteDialogOpen = isDeleteDialogOpen,
                tabSheetState = tabSheetState,
                chosenColor = chosenColor ?: Color.BLACK,
                circleColor = circleColor,
                layers = layerNames,
                layerDialogText = currentLayerDialogText ?: "",
                activeBitmap = activeBitmap,
                editIndex = layerViewEditDialogIndex
            )
        )
    }

    fun initialize() {
        if (layerNames.isEmpty()) layerNames.add("Layer 1")
        tabSheetState = tabSheetState.copy(isInitialized = false)
        updateViewState()
    }

    private fun updateColorFromHsv() {
        activeColor = hsv.toColor()
    }

    fun hsvColorUpdate(hue: Float?, saturation: Float?, value: Float?) {
        val hsvHue = hue ?: hsv.hue
        val hsvSat = saturation ?: hsv.saturation
        val hsvVal = value ?: hsv.value
        hsv = (Hsv(hsvHue, hsvSat, hsvVal))
        updateColorFromHsv()
    }

    //region getters / setters
    var hsv: Hsv
        get() = _hsv
        set(value) {
            _hsv = value
        }


    private fun setChosenColor(color: Int) {
        chosenColor = color
        updateViewState()
    }

    var currentLayerDialogText: String?
        get() = _currentLayerDialogText
        set(value) {
            _currentLayerDialogText = value ?: _currentLayerDialogText
        }

    var layerViewEditDialogIndex: Int?
        get() = _layerViewEditDialogIndex
        set(value) {
            _layerViewEditDialogIndex = value ?: _layerViewEditDialogIndex
        }

    //endregion
    //region clicked functions
    fun deleteButtonClicked(index: Int) {
        layerViewEditDialogIndex = index
        isDeleteDialogOpen = true
        updateViewState()
    }

    fun newLayerClicked() {
        isLayerDialogOpen = true
        updateViewState()
    }

    fun layerListClicked() {
        openLayerListDialog()
    }

    fun editLayerClicked(index: Int) {
        layerViewEditDialogIndex = index
        isLayerDialogOpen = true
        updateViewState()
    }

    //region submitClickFunctions
    fun submitNewLayerClicked(newString: String) {
        addLayer(newString)
    }

    fun submitColorPickerClicked(color: Int?) {
        if (color != null) {
            setChosenColor(color)
            closeColorSheet(false)
        } else {
            closeColorSheet(true)
        }
    }

    fun submitDeleteDialogClicked() {
        layerViewEditDialogIndex?.let { removeLayer(it) }
        closeDeleteDialog()
    }

    fun submitLayerEditClicked(newString: String) {
        layerViewEditDialogIndex?.let { replaceLayer(it, newString) }
    }

    fun cancelLayerDialogClicked() {
        currentLayerDialogText = ""
        closeAlertDialog()
    }

    //endregion
    //region close Click Functions
    fun layerSheetCloseClicked() {
        closeLayersSheet()
    }
    //endregion

    //endregion
    //region clicked functions Utilities

    private fun closeColorSheet(isNotSubmitted: Boolean) {
        isColorPickerSheetOpen = false
        if (!isNotSubmitted) {
            hsv = Hsv(0f, 0f, 0f)
        }
        updateViewState()
    }

    private fun closeLayersSheet() {
        isViewingLayerSheetOpen = false
        layerViewEditDialogIndex = null
        updateViewState()
    }

    private fun addLayer(layer: String) {
        layerNames.add(layer)
        currentLayerDialogText = ""
        closeAlertDialog()
    }

    private fun removeLayer(index: Int) {
        layerNames.removeAt(index)
    }

    private fun replaceLayer(index: Int, replacementLayer: String) {
        layerNames[index] = replacementLayer
        currentLayerDialogText = ""
        closeAlertDialog()
    }

    private fun closeAlertDialog() {
        isLayerDialogOpen = false
        currentLayerDialogText = ""
        updateViewState()
    }

    fun closeDeleteDialog() {
        isDeleteDialogOpen = false
        updateViewState()
    }

    private fun openLayerListDialog() {
        isViewingLayerSheetOpen = true
        updateViewState()
    }

    //endregion
    fun tabSheetChange(state: DialogUtility.TabState) {
        tabSheetState = state
        updateViewState()
    }

    fun tabSheetSlide(): DialogUtility.ToolEnum {
        return tabSheetState.selectedTool
    }

    fun getViewModelScope(): CoroutineScope {
        return this.viewModelScope
    }

}