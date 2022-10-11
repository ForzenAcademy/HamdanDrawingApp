package com.example.drawingApp.customViews

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import kotlin.math.max
import kotlin.math.min

class ForceEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : EditText(context, attrs, defStyle) {

    private val textListeners = mutableListOf<TextWatcher>()

    override fun addTextChangedListener(watcher: TextWatcher?) {
        watcher?.let { if (!textListeners.contains(it)) textListeners.add(it) }
        super.addTextChangedListener(watcher)

    }

    fun removeListeners() {
        textListeners.forEach { removeTextChangedListener(it) }
    }

    fun addBackListeners() {
        textListeners.forEach { addTextChangedListener(it) }
    }

    fun forceText(s: String) {
        if (!this.hasFocus()) {
            removeListeners()
            setText(s)
            addBackListeners()
        }
    }

    val rgbInt: Int?
        get() {
            return this.text.toString().toIntOrNull()
                ?.let { intValue -> min(max(intValue, 0), MAX_RGB) }
        }

    companion object {
        const val MAX_RGB = 255
    }

}