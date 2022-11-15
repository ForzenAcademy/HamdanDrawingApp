package com.example.drawingApp.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import androidx.core.widget.addTextChangedListener
import com.example.drawingApp.dataClasses.Hsv
import com.example.drawingApp.databinding.TabSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.max
import kotlin.math.min


class ColorPickerUtility {



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
        onSubmission: (Int?) -> Unit,
        /**
         * BottomSheet view, either use a bottomsheet or use a view acting as a bottomsheet for the view
         */
        tabSheetBinding: TabSheetBinding,
    ) {
        val behavior = BottomSheetBehavior.from(tabSheetBinding.tabSheet)
        tabSheetBinding.colorPickerTabSheet.apply {
            var previousColor = onColorTextForceUpdate().previousColor
            colorTwo.setBackgroundColor(previousColor)
            //used to set the color of the active box
            val onSettingActiveBox: (Hsv) -> Unit = { hsv ->
                val intColor = hsv.toColor()
                val red = Color.red(intColor)
                val green = Color.green(intColor)
                val blue = Color.blue(intColor)

                hslH.forceText(hsv.hue.toString())
                gradientSquare.hue = hsv.hue
                slider.sliderSetHue(hsv.hue)

                hslS.forceText((hsv.saturation * SAT_VAL_FACTOR).toString())
                gradientSquare.saturation = hsv.saturation

                hslV.forceText((hsv.value * SAT_VAL_FACTOR).toString())
                gradientSquare.value = hsv.value

                rgbR.forceText(red.toString())
                rgbG.forceText(green.toString())
                rgbB.forceText(blue.toString())
                colorHex.forceText(Integer.toHexString(Color.rgb(red, green, blue)).drop(2))
                colorOne.setBackgroundColor(intColor)

                gradientSquare.invalidate()

            }

            //called whenever text needs to be updated
            val updateForceViews = {
                val pack = onColorTextForceUpdate()
                onSettingActiveBox(pack.hsv)
            }

            colorTwo.setOnClickListener {
                //used to set the color of the active box to the previous color, impossible to be null here
                onSettingActiveBox(Hsv.fromColorToHsv(previousColor))
            }
            dialogSubmit.setOnClickListener {
                val submitColor =
                    Hsv(slider.hue, gradientSquare.saturation, gradientSquare.value).toColor()
                onSubmission(submitColor)
                previousColor = submitColor
                colorTwo.setBackgroundColor(previousColor)
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            dialogCancel.setOnClickListener {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            slider.onHueChange = {
                gradientSquare.hue = it
                slider.sliderSetHue(it)
                gradientSquare.invalidate()
                onColorUpdate(it, gradientSquare.saturation, gradientSquare.value)
                updateForceViews()
            }
            gradientSquare.onSatOrValChange = { saturation, value ->
                gradientSquare.saturation = saturation
                gradientSquare.value = value
                onColorUpdate(slider.hue, saturation, value)
                updateForceViews()
            }
            //used to prevent dragging while interacting with either gradient or slider
            gradientSquare.onDown = { behavior.isDraggable = false }
            gradientSquare.onUp = { behavior.isDraggable = true }
            slider.onDown = { behavior.isDraggable = false }
            slider.onUp = { behavior.isDraggable = true }

            hslH.addTextChangedListener {
                val hue = hslH.text.toString().toFloatOrNull()
                    ?.let { string ->
                        max(min(MAX_HUE, string), 0f)
                    }
                hue?.let {
                    onColorUpdate(hue, null, null)
                    updateForceViews()
                }
            }
            hslS.addTextChangedListener {
                val saturation = hslS.text.toString().toFloatOrNull()
                    ?.let { string -> max(min(1f, string / SAT_VAL_FACTOR), 0f) }
                saturation?.let {
                    onColorUpdate(null, saturation, null)
                    updateForceViews()
                }
            }
            hslV.addTextChangedListener {

                val value = hslV.text.toString().toFloatOrNull()
                    ?.let { string -> max(min(1f, string / SAT_VAL_FACTOR), 0f) }
                value?.let {
                    onColorUpdate(null, null, value)
                    updateForceViews()
                }
            }
            listOf(rgbR, rgbG, rgbB).forEach {
                it.addTextChangedListener {
                    if (rgbR.rgbInt != null && rgbG.rgbInt != null && rgbB.rgbInt != null) {
                        rgbToHsv(
                            rgbR.rgbInt!!,
                            rgbG.rgbInt!!,
                            rgbB.rgbInt!!
                        ).let { hsv ->
                            onColorUpdate(hsv.hue, hsv.saturation, hsv.value)
                            updateForceViews()
                        }
                    }
                }
            }
            colorHex.addTextChangedListener {
                val hex = colorHex.text.toString()
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
        }
    }
    companion object{
        const val MAX_HUE = 360f
        const val SAT_VAL_FACTOR = 100
    }
}