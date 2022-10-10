package com.example.drawingApp

import android.os.Bundle
import android.os.StrictMode
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private val model: DrawingViewModel by viewModels()

    var botSheetObj: DialogUtility.SheetObject? = null
    var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableStrictMode()
        val colorCircleView = findViewById<SelectedColorView>(R.id.colorCircle)
        val drawingFieldView = findViewById<DrawingFieldView>(R.id.drawField)
        var onSubmission: (String?) -> Boolean

        drawingFieldView.onBitmapUpdate = {
            model.activeBitmap = it
        }
        colorCircleView.onColorChange = { stateColor ->
            model.primaryColor = stateColor
            drawingFieldView.updateColor(stateColor)
        }
        //whenever we recieve a need to update the state of the view
        model.onUpdate = { state ->
            var creating = true

            state.activeBitmap?.let {
                drawingFieldView.setBitmap(it)
                drawingFieldView.invalidate()
            }
            //based on the state of the sheet alertdialog being open or closed as well as if the
            //sheet is open or closed, change how the submission function of the dialog works
            onSubmission = { layerText ->
                if (state.layers.contains(layerText)) false
                else {
                    if (state.isLayerSheetOpen && state.isAlertDialogOpen) {
                        if (botSheetObj != null && alertDialog != null) {
                            model.layerViewEditDialogIndex?.let { index ->
                                layerText?.let { layerText ->
                                    model.replaceLayer(index, layerText)
                                    botSheetObj?.onEdit?.invoke(index, layerText)
                                }
                            }
                        }
                    } else {
                        layerText?.let { model.addLayer(it) }
                    }
                    alertDialog?.dismiss()
                    alertDialog = null
                    model.currentLayerDialogText = ""
                    model.closeAlertDialog()
                    true
                }
            }
            //if the sheet is open, open it with the correct information
            if (state.isLayerSheetOpen && botSheetObj == null) {
                botSheetObj = DialogUtility.showViewingLayersDialog(
                    context = this,
                    layerNames = state.layers,
                    onEdit = {
                        model.layerViewEditDialogIndex = it
                        model.openAlertDialog()
                    },
                    onDelete = { model.removeLayer(it) },
                    onSheetDismiss = {
                        botSheetObj?.sheet?.dismiss()
                        botSheetObj=null
                        model.closeLayersSheet()
                    }
                )
            }
            //if the dialog is open, open it with the correct type of submission
            if (state.isAlertDialogOpen && alertDialog == null) {
                alertDialog = DialogUtility.showLayerAlertDialog(
                    context = this,
                    startString = hintText(state.layers, state.layerDialogText),
                    isCreatingNewLayer = creating,
                    onTextChanged = { model.currentLayerDialogText = it },
                    onDialogSubmission = onSubmission,
                )
            }

        }

        findViewById<TextView>(R.id.newLayer).setOnClickListener {
            model.openAlertDialog()
        }

        findViewById<TextView>(R.id.editLayer).setOnClickListener {
            model.openLayersSheet()
        }

        model.initialize()

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

    fun hintText(layers: List<String>, refString: String): String {
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

}