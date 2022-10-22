package com.example.drawingApp

import android.os.Bundle
import android.os.StrictMode
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.drawingApp.customViews.ColorCircleView
import com.example.drawingApp.customViews.DrawingFieldView
import com.example.drawingApp.utils.ColorPickerUtility
import com.example.drawingApp.utils.DialogUtility
import com.example.drawingApp.utils.ImageUtility


class MainActivity : AppCompatActivity() {
    private val model: DrawingViewModel by viewModels()
    private var isColorSheetOpen = false

    var botSheetObj: DialogUtility.SheetObject? = null
    var alertDialog: AlertDialog? = null
    var deleteDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableStrictMode()
        val colorCircleView = findViewById<ColorCircleView>(R.id.colorCircle)
        val drawingFieldView = findViewById<DrawingFieldView>(R.id.drawField)
        val getGalleryImageView = findViewById<ImageView>(R.id.getImageButton)
        var onSubmission: (String?) -> Boolean
        val imageContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri ->
                ImageUtility.getBitmapFromUri(uri, this, {
                    Toast.makeText(this, "Error getting Bitmap from URI", Toast.LENGTH_SHORT)
                }) { bitmap ->
                    drawingFieldView.setBitmapFromImageBitmap(bitmap)
                }
            }
        }

        getGalleryImageView.setOnClickListener {
            launchGalleryImageGetter(imageContent)
        }
        drawingFieldView.onBitmapUpdate = {
            model.activeBitmap = it
        }
        colorCircleView.onColorChange = { stateColor ->
            model.circleColor = stateColor
            drawingFieldView.setPaintColor(stateColor)
        }
        //whenever we receive a need to update the state of the view
        model.onUpdate = { state ->
            state.activeBitmap?.let {
                drawingFieldView.setBitmap(it)
                drawingFieldView.invalidate()
            }
            DialogUtility.tabSheetDialog(
                findViewById(R.id.tabSheet),
                onInit = { state.tabSheetState },
                onStateChanged = { model.tabSheetSlide(it) })
            //based on the state of the sheet alertdialog being open or closed as well as if the
            //sheet is open or closed, change how the submission function of the dialog works
            onSubmission = { layerText ->
                if (state.layers.contains(layerText)) false
                else {
                    if (state.isLayerSheetOpen && state.isLayerDialogOpen) {
                        if (botSheetObj != null && alertDialog != null) {
                            layerText?.let {
                                model.submitLayerEditClicked(layerText)
                                model.layerViewEditDialogIndex?.let {
                                    botSheetObj?.onEdit?.invoke(it, layerText)
                                }
                            }
                        }
                    } else {
                        layerText?.let {
                            model.submitNewLayerClicked(it)
                        }
                    }
                    alertDialog?.dismiss()
                    alertDialog = null
                    model.cancelLayerDialogClicked()
                    true
                }
            }
            //if the sheet is open, open it with the correct information
            if (state.isLayerSheetOpen && botSheetObj == null) {
                botSheetObj = DialogUtility.showViewingLayersDialog(
                    context = this,
                    layerNames = state.layers,
                    onEdit = {
                        model.editLayerClicked(it)
                    },
                    onDelete = {
                        model.deleteButtonClicked(it)
                    },
                    onSheetDismiss = {
                        botSheetObj?.sheet?.dismiss()
                        botSheetObj = null
                        model.layerSheetCloseClicked()
                    }
                )
            }
            //if the dialog is open, open it with the correct type of submission
            if (state.isLayerDialogOpen && alertDialog == null) {
                alertDialog = DialogUtility.showLayerAlertDialog(
                    context = this,
                    startString = hintText(state.layers, state.layerDialogText),
                    isCreatingNewLayer = !state.isLayerSheetOpen,   //way to check if were editing or creating a layer
                    onTextChanged = { model.currentLayerDialogText = it },
                    onDialogSubmission = onSubmission,
                )
            }
            //if the gradient is supposed to be open, open it with the previous state it was in
            if (state.isColorSheetOpen && !isColorSheetOpen) {
                isColorSheetOpen = true
                ColorPickerUtility.colorPickerSheet(context = this,
                    onColorUpdate = { hue, saturation, value ->
                        model.hsvColorUpdate(hue, saturation, value)
                    },
                    onColorTextForceUpdate = {
                        ColorPickerUtility.ColorPack(
                            model.hsv,
                            previousColor = state.chosenColor,
                        )
                    },
                    onSubmission = { color ->
                        model.submitColorPickerClicked(color)
                        if (color != null) Toast.makeText(
                            this,
                            "Chosen Color: $color",
                            Toast.LENGTH_SHORT
                        ).show()
                        isColorSheetOpen = false
                    }
                )
            }
            //if the delete dialog is open
            if (state.isDeleteDialogOpen && deleteDialog == null) {
                deleteDialog = DialogUtility.showDeleteDialog(
                    context = this,
                    onDismiss = {
                        model.closeDeleteDialog()
                        deleteDialog = null
                    },
                    onSubmission = {
                        model.submitDeleteDialogClicked()
                        model.layerViewEditDialogIndex?.let { index ->
                            botSheetObj?.onDelete?.invoke(index)
                        }
                        deleteDialog = null
                    }
                )
            }

            //used to persist the state of the color circle, and the actively drawn color
            colorCircleView.setColor(state.circleColor)
            colorCircleView.onColorChange?.let { it(state.circleColor) }
        }

        findViewById<TextView>(R.id.newLayer).setOnClickListener {
            model.newLayerClicked()
        }

        findViewById<TextView>(R.id.colorPickerBtn).setOnClickListener {
            model.colorPickerClicked()
        }

        findViewById<TextView>(R.id.editLayer).setOnClickListener {
            model.layerListClicked()
        }



        model.initialize()
    }

    /**
     * launches with the purpose of obtaining an image, will open the users gallery
     */
    private fun launchGalleryImageGetter(contentLauncher: ActivityResultLauncher<String>) {
        contentLauncher.launch("image/*")
    }

    private fun hintText(layers: List<String>, refString: String): String {
        if (refString != "") return refString
        var layerCounter = 1
        while (layers.contains(
                resources.getString(
                    R.string.new_layer_hint,
                    layerCounter
                )
            )
        ) {
            layerCounter += 1
        }
        return resources.getString(R.string.new_layer_hint, layerCounter)
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        );
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                //.detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build()
        );
    }

}