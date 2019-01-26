package com.unsplash.pickerandroid.photopicker.presentation

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Image(
    val authorName: String,
    val thumbnailUrl: String,
    val smallUrl: String,
    val regularUrl: String,
    val fullUrl: String,
    val rawUrl: String,
    val downloadUrl: String
) : Parcelable
