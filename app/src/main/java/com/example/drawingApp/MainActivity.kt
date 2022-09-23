package com.example.drawingApp

import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener

class MainActivity : AppCompatActivity() {
    private val model: DrawingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableStrictMode()
        val colorCircleView = findViewById<SelectedColorView>(R.id.colorCircle)
        val drawingFieldView = findViewById<DrawingFieldView>(R.id.drawField)

        drawingFieldView.onBitmapUpdate = {
            model.activeBitmap = it
        }

        findViewById<TextView>(R.id.newLayer).setOnClickListener {
            showLayerAlertDialog()
        }

        model.activeBitmap?.let {
            drawingFieldView.setBitmap(it)
            drawingFieldView.invalidate()
        }

        colorCircleView.onColorChange = { stateColor ->
            model.primaryColor = stateColor
            drawingFieldView.updateColor(stateColor)
        }

        if (model.isLayerCreationDialogOpen) showLayerAlertDialog()

    }

    fun showLayerAlertDialog() {
        val inflater = LayoutInflater.from(this@MainActivity)
        val view = inflater.inflate(R.layout.layer_dialog, null)
        val layerField = view.findViewById<EditText>(R.id.newLayerField)
        val errorText = view.findViewById<TextView>(R.id.dialogError)
        var layerCounter = 1

        //find the first available layer name not taken
        while (model.layerNames.contains(getString(R.string.new_layer_hint, layerCounter))) {
            layerCounter += 1
        }
        layerField.setText(getString(R.string.new_layer_hint, layerCounter))

        //needs to be after the set text or else the textchanged listener will overwrite the previous text needed for rotations
        layerField.addTextChangedListener {
            val fieldString = it.toString().trim()
            model.currentLayerCreationDialogText = fieldString
            errorText.setText(R.string.charCountError)
            errorText.isVisible = fieldString.length > MAX_CHAR_LEN
        }

        val dialog = AlertDialog.Builder(this).apply {
            setView(view)
        }.create()

        view.findViewById<TextView>(R.id.newLayerCancel).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<TextView>(R.id.newLayerOk).setOnClickListener {
            val fieldString = layerField.text.toString().trim()
            if (fieldString.length > MAX_CHAR_LEN) {
                errorText.visibility = View.VISIBLE
            } else if (model.layerNames.contains(fieldString)) {
                errorText.setText(R.string.layerExistsError)
                errorText.visibility = View.VISIBLE
            } else {
                model.layerNames.add(fieldString)
                Toast.makeText(this@MainActivity, fieldString, Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            }
        }
        //this is here so that in the case that the dialogue was previously open
        //we will immediately know to set the text to what it was previously
        //the reason it is here, is so that we avoid overriding the correct text
        if (model.isLayerCreationDialogOpen) {
            layerField.setText(model.currentLayerCreationDialogText)
        }
        model.isLayerCreationDialogOpen = true
        dialog.setOnDismissListener { model.isLayerCreationDialogOpen = false }
        dialog.show()

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

    companion object {
        private val MAX_CHAR_LEN = 16
    }
}