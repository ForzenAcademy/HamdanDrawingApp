package com.example.drawingApp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.drawingApp.customViews.DrawingFieldView
import com.example.drawingApp.utils.ColorPickerUtility
import com.example.drawingApp.utils.DialogUtility
import com.example.drawingApp.utils.ImageUtility


class MainActivity : AppCompatActivity() {
    private val model: DrawingViewModel by viewModels()

    var botSheetObj: DialogUtility.SheetObject? = null
    var alertDialog: AlertDialog? = null
    var deleteDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableStrictMode()
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
        var stateColor = Color.BLACK

        getGalleryImageView.setOnClickListener {
            launchGalleryImageGetter(imageContent)
        }
        drawingFieldView.onBitmapUpdate = {
            model.activeBitmap = it
        }
        //whenever we receive a need to update the state of the view
        model.onUpdate = { state ->
            stateColor = state.chosenColor

            state.activeBitmap?.let {
                drawingFieldView.setBitmap(it)
                drawingFieldView.invalidate()
            }
            drawingFieldView.setPaintColor(state.chosenColor)
            DialogUtility.tabSheetDialog(
                findViewById(R.id.tabSheet),
                state = state.tabSheetState,
                onSheetStateChanged = { model.tabSheetChange(it) },
                onSheetSlide = { model.tabSheetSlide() },
            )
            //sets the color of the gradient tab button
            findViewById<LinearLayout>(R.id.tabSheet).findViewById<ImageView>(R.id.colorGradientColor).imageTintList =
                ColorStateList.valueOf(state.chosenColor)
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
        }

        findViewById<TextView>(R.id.newLayer).setOnClickListener {
            model.newLayerClicked()
        }

        findViewById<TextView>(R.id.editLayer).setOnClickListener {
            model.layerListClicked()
        }

        ColorPickerUtility.colorPickerSheet(
            onColorUpdate = { hue, saturation, value ->
                model.hsvColorUpdate(hue, saturation, value)
            },
            onColorTextForceUpdate = {
                ColorPickerUtility.ColorPack(
                    model.hsv,
                    previousColor = stateColor,
                )
            },
            onSubmission = { color ->
                model.submitColorPickerClicked(color)
                if (color != null) Toast.makeText(
                    this,
                    "Chosen Color: $color",
                    Toast.LENGTH_SHORT
                ).show()
            },
            view = findViewById<LinearLayout>(R.id.tabSheet)
        )

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