package com.unsplash.pickerandroid.photopicker.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UnsplashPhoto(
    val id: String,
    val created_at: String,
    val width: Int,
    val height: Int,
    val color: String? = "#000000",
    val likes: Int,
    val description: String?,
    val urls: UnsplashUrls,
    val links: UnsplashLinks,
    val user: UnsplashUser
) : Parcelable
