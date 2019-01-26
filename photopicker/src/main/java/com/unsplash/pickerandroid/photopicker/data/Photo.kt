package com.unsplash.pickerandroid.photopicker.data

data class Photo(
    val width: Int,
    val height: Int,
    val color: String,
    val urls: Urls,
    val links: Links,
    val user: User
) {
    data class Urls(
        val thumb: String,
        val small: String,
        val regular: String,
        val full: String,
        val raw: String
    )

    data class Links(
        val download: String
    )

    data class User(
        val name: String
    )
}
