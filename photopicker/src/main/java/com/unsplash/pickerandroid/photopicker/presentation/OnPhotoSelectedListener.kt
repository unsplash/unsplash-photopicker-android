package com.unsplash.pickerandroid.photopicker.presentation

import android.widget.ImageView

interface OnPhotoSelectedListener {

    /**
     * When a specified number of photos are selected.
     *
     * @param nbOfSelectedPhotos the number of selected photos
     */
    fun onPhotoSelected(nbOfSelectedPhotos: Int)

    /**
     * When the user long presses an photo.
     *
     * @param imageView the long pressed image view
     * @param url the url of the photo
     */
    fun onPhotoLongPress(imageView: ImageView, url: String)
}
