package com.unsplash.pickerandroid.photopicker.data

data class SearchResponse(
    val total: Int,
    val total_pages: Int,
    val results: List<UnsplashPhoto>
)
