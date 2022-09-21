package com.example.drawingApp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val colorCircleView = findViewById<SelectedColorView>(R.id.colorCircle)
        val drawingFieldView = findViewById<DrawingFieldView>(R.id.drawField)

        colorCircleView.onColorChange = { stateColor ->
            model.primaryColor = stateColor
            drawingFieldView.updateColor(stateColor)
        }

    }
}