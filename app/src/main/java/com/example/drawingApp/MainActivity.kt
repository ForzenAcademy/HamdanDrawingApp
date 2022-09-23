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
import androidx.core.widget.addTextChangedListener

class MainActivity : AppCompatActivity() {
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableStrictMode()
        val colorCircleView = findViewById<SelectedColorView>(R.id.colorCircle)
        val drawingFieldView = findViewById<DrawingFieldView>(R.id.drawField)

        drawingFieldView.onFinishLine = {
            model.activeBitmap = it
        }
        findViewById<TextView>(R.id.newLayer).setOnClickListener {
            showLayerAlertDialog()
        }
        if (model.activeBitmap != null) {
            model.activeBitmap?.let { it -> drawingFieldView.setBitmap(it) }
            drawingFieldView.invalidate()
        }

        colorCircleView.onColorChange = { stateColor ->
            model.primaryColor = stateColor
            drawingFieldView.updateColor(stateColor)
        }

        if (model.dialogOpen) showLayerAlertDialog()
        //model.viewModelScope

    }

    fun showLayerAlertDialog() {
        val inflater = LayoutInflater.from(this@MainActivity)
        val view = inflater.inflate(R.layout.layer_dialog, null)
        val layerField = view.findViewById<EditText>(R.id.newLayerField)
        val errorText = view.findViewById<TextView>(R.id.dialogCharCountError)

        //if the layer number is not in the list already then set it to the field
        if (model.layers.indexOf("Layer ${model.layerCounter}") == -1) {
            layerField.setText(getString(R.string.new_layer_hint, model.layerCounter))
        } else {
            //else if it is in the list, keep looping till we find that its not
            while (model.layers.indexOf("Layer ${model.layerCounter}") != -1) {
                model.layerCounter += 1
            }
            layerField.setText(getString(R.string.new_layer_hint, model.layerCounter))
        }
        //needs to be after the if or else the textchanged listener will overwrite the previous text needed for rotations
        layerField.addTextChangedListener {
            model.dialogText = layerField.text.toString()
            if (it.toString().trim().length > 16) {
                errorText.visibility = View.VISIBLE
                layerField.setText(layerField.text.toString().dropLast(1))
            }
        }

        val dialog = AlertDialog.Builder(this).apply {
            setView(view)
        }.create()

        view.findViewById<TextView>(R.id.newLayerCancel).setOnClickListener {
            dialog.dismiss()
        }
        view.findViewById<TextView>(R.id.newLayerOk).setOnClickListener {
            errorText.visibility = View.INVISIBLE
            if (layerField.text.toString() ==
                getString(R.string.new_layer_hint, model.layerCounter)
            ) model.layerCounter += 1
            model.layers.add(layerField.text.toString())
            Toast.makeText(this@MainActivity, layerField.text.toString(), Toast.LENGTH_SHORT)
                .show()
            dialog.dismiss()
        }
        if (model.dialogOpen) {
            layerField.setText(model.dialogText)
        }
        model.dialogOpen = true
        dialog.setOnDismissListener { model.dialogOpen = false }
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
}