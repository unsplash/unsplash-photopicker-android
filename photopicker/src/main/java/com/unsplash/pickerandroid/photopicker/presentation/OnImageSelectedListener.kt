package com.unsplash.pickerandroid.photopicker.presentation

import android.widget.ImageView

interface OnImageSelectedListener {

    fun onImageSelected(nbOfSelectedImages: Int)

    fun onImageLongPress(imageView: ImageView, url: String)
}
