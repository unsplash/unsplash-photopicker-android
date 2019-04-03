package com.unsplash.pickerandroid.photopicker.presentation

import android.widget.ImageView

interface OnImageSelectedListener {

    /**
     * When a specified number of images are selected.
     *
     * @param nbOfSelectedImages the number of selected images
     */
    fun onImageSelected(nbOfSelectedImages: Int)

    /**
     * When the user long presses an image.
     *
     * @param imageView the long pressed image view
     * @param url the url of the photo
     */
    fun onImageLongPress(imageView: ImageView, url: String)
}
