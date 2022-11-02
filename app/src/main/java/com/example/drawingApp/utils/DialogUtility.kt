package com.example.drawingApp.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drawingApp.EditLayerAdapter
import com.example.drawingApp.LayerViewModel
import com.example.drawingApp.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.color.MaterialColors

object DialogUtility {

    const val MAX_CHAR_LEN = 16

    /**
     * used to update the sheet adapter with the onEdit and onDelete lambda
     * the sheet is for dismissing to avoid window leaks
     */
    data class SheetObject(
        val sheet: BottomSheetDialog,
        val onEdit: (Int, String) -> Unit,
        val onDelete: (Int) -> Unit
    )

    data class SheetTool(
        val btn: Int, val btnTab: Int, val body: Int, val tool: ToolEnum
    )

    enum class ToolEnum {
        Gradient, Brush, Move, Resize, Filters, Layers
    }

    data class TabState(
        val tabState: Int,
        val selectedTool: SheetTool,
        val isInitialized: Boolean
    )

    fun tabSheetDialog(
        view: LinearLayout,
        /**
         * used to set the initial state of the tab sheet, primarily used for maintaining in rotation
         * also used to decide if the state needs to be updated in the view
         */
        state: TabState,
        /**
         * called whenever the state should be changed, this should set wherever we are keeping the behavior state of the tab
         * meaning whether its expanded or collapsed as well as what tool is selected
         */
        onSheetStateChanged: (state: TabState) -> Unit,
        /**
         * used to gurantee correct tool is selected when sliding up or down
         */
        onSheetSlide: () -> SheetTool,
    ) {
        val behavior = BottomSheetBehavior.from(view)
        var selectedTool = state.selectedTool
        behavior.state = state.tabState
        val tools = listOf(
            SheetTool(
                R.id.colorGradientBtn,
                R.id.colorGradientTab,
                R.id.colorPickerLayout,
                ToolEnum.Gradient
            ),
            SheetTool(
                R.id.brushSettingsBtn,
                R.id.brushSettingsTab,
                R.id.hideableBrush,
                ToolEnum.Brush
            ),
            SheetTool(R.id.moveImageBtn, R.id.moveImageTab, R.id.hideableMove, ToolEnum.Move),
            SheetTool(R.id.resizeBtn, R.id.resizeTab, R.id.hideableResize, ToolEnum.Resize),
            SheetTool(R.id.filtersBtn, R.id.filtersTab, R.id.hideableFilter, ToolEnum.Filters),
            SheetTool(R.id.layersBtn, R.id.layersTab, R.id.hideableLayer, ToolEnum.Layers)
        )
        val callback = object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_EXPANDED) {
                    onSheetStateChanged(
                        state.copy(
                            tabState = newState,
                            selectedTool = onSheetSlide(),
                            isInitialized = true
                        ),
                    )
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }
        val onSheetButtonClicked: (tool: SheetTool) -> Unit = {
            tools.forEach { toolItem ->
                view.findViewById<View>(toolItem.body).clearAnimation()
                view.findViewById<View>(toolItem.btnTab).backgroundTintList =
                    ColorStateList.valueOf(
                        Color.WHITE
                    )
            }
            selectedTool = it
            showTabContent(view, tools, selectedTool)
            onSheetStateChanged(
                state.copy(
                    tabState = BottomSheetBehavior.STATE_EXPANDED,
                    selectedTool = selectedTool,
                    isInitialized = true,
                ),
            )
        }
        tools.forEach { tool ->
            view.findViewById<View>(tool.btn).setOnClickListener {
                onSheetButtonClicked(tool)
            }
        }

        if (!state.isInitialized) {
            showTabContent(view, tools, state.selectedTool)
            //used to avoid animations from playing when sheet is changed from expanded or collapsed and on rotation
            view.children.forEach { it.clearAnimation() }
            behavior.addBottomSheetCallback(callback)
        }
    }

    fun showTabContent(view: View, tools: List<SheetTool>, tool: SheetTool) {
        tools.forEach {
            view.findViewById<View>(it.body).apply {
                isVisible = (it == tool)
                if (it == tool) {
                    this.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.fade_in))
                    view.findViewById<View>(it.btnTab).backgroundTintList =
                        ColorStateList.valueOf(
                            MaterialColors.getColor(
                                view,
                                R.attr.selectedTabColorHamdan
                            )
                        )
                }
            }
        }
    }

    /**
     * opens a dialog that will take a string and allows you to interact with the string
     * returns the string to wherever on the onSubmission.
     * isCreating is to tell what the dialog is used for, if its for creating a new layer or editing one.
     * This is purely used to update the title on the dialog.
     */
    fun showLayerAlertDialog(
        context: Context,
        startString: String = "",
        isCreatingNewLayer: Boolean = true,
        /**
         * what to do whenever the text in the field is changed
         * passes the string from the field
         */
        onTextChanged: (String) -> Unit,
        /**
         * what to do on submission
         * uses null on dismiss listener
         * intended to set the string to wherever it needs to be
         * and always closes the dialog
         */
        onDialogSubmission: (String?) -> Boolean,
        /**
         * on opening of the dialog
         * if you need to set any variables to know that its open
         * do it here, or if you need something to happen as it opens
         */
    ): AlertDialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layer_dialog, null)
        val layerField = view.findViewById<EditText>(R.id.newLayerField)
        val errorText = view.findViewById<TextView>(R.id.dialogError)
        val titleText = view.findViewById<TextView>(R.id.layerDialogTitle)
        if (isCreatingNewLayer) titleText.setText(R.string.new_layer_dialog_title)
        else titleText.setText(R.string.edit_layer_dialog_title)

        layerField.setText(startString)

        //this text changed listener needs to be after the set text
        //checks validity of the string length in the layer field, if not valid shows an error
        layerField.addTextChangedListener {
            val fieldString = it.toString().trim()
            onTextChanged(fieldString)
            errorText.setText(R.string.charCountError)
            errorText.isVisible = fieldString.length > MAX_CHAR_LEN
        }

        val dialog = AlertDialog.Builder(context).apply {
            setView(view)
        }.create()

        view.findViewById<TextView>(R.id.newLayerCancel).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<TextView>(R.id.newLayerOk).setOnClickListener {
            val fieldString = layerField.text.toString().trim()
            errorText.isVisible = fieldString.length > MAX_CHAR_LEN
            if (!errorText.isVisible) {
                if (!onDialogSubmission(fieldString) && !errorText.isVisible) {
                    errorText.setText(R.string.layerExistsError)
                    errorText.isVisible = !errorText.isVisible
                } else {
                    Toast.makeText(context, fieldString, Toast.LENGTH_SHORT)
                        .show()
                    dialog.dismiss()
                }
            }
        }

        dialog.setOnDismissListener {
            onDialogSubmission(null)
        }
        dialog.show()
        return dialog
    }

    /**
     * opens a sheet dialog that allows the viewing of the layers in the list
     * allows editing and deletion of items in the list
     */
    fun showViewingLayersDialog(
        context: Context,
        layerNames: List<String>,
        /**
         * called when the edit of a layer is clicked with the index of that layer
         * ideally set the index to some variable to work with later
         */
        onEdit: (Int) -> Unit,
        /**
         * what happens when user clicks the delete button on a layer
         * called in the Delete click
         * remove the item at the index from the list your passing in
         */
        onDelete: (Int) -> Unit,
        /**
         * called on dismissals, do whatever tracking needed when the sheet closes
         */
        onSheetDismiss: () -> Unit
    ): SheetObject {
        val data = layerNames.map { layer -> LayerViewModel(layer) }.toMutableList()

        lateinit var myAdapter: EditLayerAdapter
        myAdapter = EditLayerAdapter(
            data = data,
            onDeleteClick = { layerModel ->
                val index = data.indexOf(layerModel)
                onDelete(index)
            },
            onEditClick = { layerModel ->
                val index = data.indexOf(layerModel)
                onEdit(index)
            }
        )

        val sheet = BottomSheetDialog(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.edit_layer_recycler, null)
        view.findViewById<RecyclerView>(R.id.editLayerRecycler).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myAdapter
        }
        view.findViewById<TextView>(R.id.closeLayerView).setOnClickListener { sheet.dismiss() }

        sheet.setContentView(view)
        sheet.setOnDismissListener { onSheetDismiss() }
        sheet.show()

        return SheetObject(
            sheet = sheet,
            onEdit = { index, str ->
                data[index] = LayerViewModel(str)
                myAdapter.notifyItemChanged(index)
            }
        ) { index ->
            data.removeAt(index)
            myAdapter.notifyItemRemoved(index)
        }
    }

    /**
     * Shows a dialog to confirm or cancel a deletion
     * calls onDismiss whenever the dialog is closed, ideally use this to let yourself know that the dialog is closed so its not open on rotation
     * calls onSubmission on positive button press
     */
    fun showDeleteDialog(
        context: Context,
        /**
         * occurs whenever the view is closed, use to set external variables to whatever you need when the dialog is closed
         */
        onDismiss: () -> Unit,
        /**
         * calls when confirm is pressed, use to actualy perform the actions you want taken on submit
         */
        onSubmission: () -> Unit
    ): AlertDialog {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(R.string.deleting)
        dialog.setMessage(R.string.delete_confirmation_text)
        dialog.apply {
            setPositiveButton(R.string.confirm) { _, _ ->
                onSubmission()
            }
            setNegativeButton(R.string.cancel) { _, _ -> }
        }
        val alert = dialog.create()
        dialog.setOnDismissListener {
            onDismiss()
        }
        dialog.show()
        return alert
    }
}