package com.example.drawingApp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.drawingApp.R

class ImageUtility {
    /**
     * function that takes a Uri and gets a bitmap from it from the gallery.
     */
    fun getBitmapFromUri(
        uri: Uri,
        context: Context,
        onError: () -> Unit,
        onBitmapLoaded: (Bitmap) -> Unit
    ) {
        Glide
            .with(context)
            .asBitmap()
            .error(
                Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.ic_launcher_background)
            )
            .load(uri)
            .into(BitmapGetter(onError = onError) { onBitmapLoaded(it) })
    }
}

/**
 * onErrorBitmap will just load a drawable from resources as a bitmap
 * onGetBitmap will grab the bitmap for putting wherever its needed
 */
class BitmapGetter(val onError: () -> Unit, val onGetBitmap: (Bitmap) -> Unit) :
    CustomTarget<Bitmap>() {
    override fun onLoadCleared(placeholder: Drawable?) {}
    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        try {
            onGetBitmap(resource)
        } catch (e: Exception) {
            onError()
        }

    }
}