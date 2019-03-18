package com.unsplash.pickerandroid.photopicker.presentation

import android.widget.ImageView

interface OnImageSelectedListener {

    /**
     * When a specified number of images are selected.
     *
     * @param nbOfSelectedImages the number of selected images
     * @param userInput true if the user is the one that has selected the photos, false otherwise
     */
    fun onImageSelected(nbOfSelectedImages: Int, userInput: Boolean)

    /**
     * When the user long presses an image.
     *
     * @param imageView the long pressed image view
     * @param url the url of the photo
     */
    fun onImageLongPress(imageView: ImageView, url: String)
}
