package com.unsplash.pickerandroid.photopicker.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UnsplashLinks(
    val self: String,
    val html: String,
    val photos: String?,
    val likes: String?,
    val portfolio: String?,
    val download: String?,
    val download_location: String?
) : Parcelable
