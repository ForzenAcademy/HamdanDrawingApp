package com.example.drawingApp.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.example.drawingApp.*
import com.example.drawingApp.CustomViews.ColorSlider
import com.example.drawingApp.CustomViews.ForceEditText
import com.example.drawingApp.CustomViews.GradientSquare
import com.example.drawingApp.DataClasses.Hsv
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.max
import kotlin.math.min

object ColorPickerUtility {
    const val MAX_HUE = 360f
    const val SAT_VAL_FACTOR = 100

    data class ColorPack(
        val hsv: Hsv,
        val previousColor: Int
    )

    fun rgbToHsv(red: Int, green: Int, blue: Int): Hsv {
        val hsv = FloatArray(3)
        Color.RGBToHSV(red, green, blue, hsv)
        return Hsv(hsv[0], hsv[1], hsv[2])
    }

    @SuppressLint("ClickableViewAccessibility")
    fun colorPickerSheet(
        context: Context,
        /**
         * Called whenever the hue is updated via text or slider.
         * Use it to set wherever you are storing your hue
         * and then update your color
         */
        onColorUpdate: (hue: Float?, saturation: Float?, value: Float?) -> Unit,
        /**
         * Called whenever text needs to be updated, meaning on any change, hsv, rgb, or text.
         * Used to give the information of hsv and previous color.
         * if there was no previous color pass it black. This is used to help
         * populate the sheets text views and update the slider and gradient indicators as well as set the previous color box.
         */
        onColorTextForceUpdate: () -> ColorPack,
        /**
         * called whenever the dialog is dismissed or submitted
         * on actual submission the color will not be null so take it in and set it wherever you need the selected color and set the hsv and rgb fields to defaults.
         * on null don't do anything with it, don't set rgb and hsv to defaults.
         */
        onSubmission: (Int?) -> Unit
    ) {
        val sheet = BottomSheetDialog(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.color_picker_bottom_sheet, null)
        val submit = view.findViewById<TextView>(R.id.dialogSubmit)
        val cancel = view.findViewById<TextView>(R.id.dialogCancel)
        val hueView = view.findViewById<ForceEditText>(R.id.hslH)
        val saturationView = view.findViewById<ForceEditText>(R.id.hslS)
        val valueView = view.findViewById<ForceEditText>(R.id.hslV)
        val rgbRedView = view.findViewById<ForceEditText>(R.id.rgbR)
        val rgbGreenView = view.findViewById<ForceEditText>(R.id.rgbG)
        val rgbBlueView = view.findViewById<ForceEditText>(R.id.rgbB)
        val colorHexView = view.findViewById<ForceEditText>(R.id.colorHex)
        val slider = view.findViewById<ColorSlider>(R.id.slider)
        val square = view.findViewById<GradientSquare>(R.id.gradientSquare)
        val activeColorBox = view.findViewById<TextView>(R.id.colorOne)
        val previousColorBox = view.findViewById<TextView>(R.id.colorTwo)
        val previousColor = onColorTextForceUpdate().previousColor
        previousColorBox.setBackgroundColor(previousColor)
        //used to set the color of the active box
        val onSettingActiveBox: (Hsv) -> Unit = { hsv ->
            val intColor = hsv.toColor()
            val red = Color.red(intColor)
            val green = Color.green(intColor)
            val blue = Color.blue(intColor)

            hueView.forceText(hsv.hue.toString())
            square.hue = hsv.hue
            slider.sliderSetHue(hsv.hue)

            saturationView.forceText((hsv.saturation * SAT_VAL_FACTOR).toString())
            square.saturation = hsv.saturation

            valueView.forceText((hsv.value * SAT_VAL_FACTOR).toString())
            square.value = hsv.value

            rgbRedView.forceText(red.toString())
            rgbGreenView.forceText(green.toString())
            rgbBlueView.forceText(blue.toString())
            colorHexView.forceText(Integer.toHexString(Color.rgb(red, green, blue)).drop(2))
            activeColorBox.setBackgroundColor(intColor)

            square.invalidate()

        }

        //called whenever text needs to be updated
        val updateForceViews = {
            val pack = onColorTextForceUpdate()
            onSettingActiveBox(pack.hsv)
        }

        previousColorBox.setOnClickListener {
            //used to set the color of the active box to the previous color, impossible to be null here
            onSettingActiveBox(Hsv.fromColorToHsv(previousColor))
        }
        submit.setOnClickListener {
            onSubmission(
                Hsv(slider.hue, square.saturation, square.value).toColor()
            )
            sheet.dismiss()
        }
        cancel.setOnClickListener {
            sheet.dismiss()
        }

        slider.onHueChange = {
            square.hue = it
            slider.sliderSetHue(it)
            square.invalidate()
            onColorUpdate(it, square.saturation, square.value)
            updateForceViews()
        }
        square.onSatOrValChange = { saturation, value ->
            square.saturation = saturation
            square.value = value
            onColorUpdate(slider.hue, saturation, value)
            updateForceViews()
        }
        //used to prevent dragging while interacting with either gradient or slider
        square.onDown = { sheet.behavior.isDraggable = false }
        square.onUp = { sheet.behavior.isDraggable = true }
        slider.onDown = { sheet.behavior.isDraggable = false }
        slider.onUp = { sheet.behavior.isDraggable = true }

        hueView.addTextChangedListener {
            val hue = hueView.text.toString().toFloatOrNull()
                ?.let { string ->
                    max(min(MAX_HUE, string), 0f)
                }
            hue?.let {
                onColorUpdate(hue, null, null)
                updateForceViews()
            }
        }
        saturationView.addTextChangedListener {
            val saturation = saturationView.text.toString().toFloatOrNull()
                ?.let { string -> max(min(1f, string / SAT_VAL_FACTOR), 0f) }
            saturation?.let {
                onColorUpdate(null, saturation, null)
                updateForceViews()
            }
        }
        valueView.addTextChangedListener {
            val value = valueView.text.toString().toFloatOrNull()
                ?.let { string -> max(min(1f, string / SAT_VAL_FACTOR), 0f) }
            value?.let {
                onColorUpdate(null, null, value)
                updateForceViews()
            }
        }
        listOf(rgbRedView, rgbGreenView, rgbBlueView).forEach {
            it.addTextChangedListener {
                if (rgbRedView.rgbInt != null && rgbGreenView.rgbInt != null && rgbBlueView.rgbInt != null) {
                    rgbToHsv(
                        rgbRedView.rgbInt!!,
                        rgbGreenView.rgbInt!!,
                        rgbBlueView.rgbInt!!
                    ).let { hsv ->
                        onColorUpdate(hsv.hue, hsv.saturation, hsv.value)
                        updateForceViews()
                    }
                }
            }
        }
        colorHexView.addTextChangedListener {
            val hex = colorHexView.text.toString()
            try {
                val color = Color.parseColor("#$hex")
                val hsv = FloatArray(3)
                Color.colorToHSV(color, hsv)
                slider.sliderSetHue(hsv[0])
                onColorUpdate(hsv[0], hsv[1], hsv[2])
                updateForceViews()
            } catch (error: Exception) {
                Log.v("hex error", "Error: $error")
            }
        }

        updateForceViews()
        sheet.setOnDismissListener {
            onSubmission(null)
        }
        sheet.setContentView(view)
        sheet.show()
    }

}