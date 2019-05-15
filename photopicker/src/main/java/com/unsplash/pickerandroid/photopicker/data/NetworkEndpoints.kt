package com.unsplash.pickerandroid.photopicker.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Retrofit endpoints definition.
 */
interface NetworkEndpoints {

    @GET("collections/317099/photos")
    fun loadPhotos(
        @Query("client_id") clientId: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int
    ): Call<List<UnsplashPhoto>>

    @GET("search/photos")
    fun searchPhotos(
        @Query("client_id") clientId: String,
        @Query("query") criteria: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int
    ): Call<SearchResponse>

    @GET
    fun trackDownload(@Url url: String): Call<Void>

    companion object {
        const val BASE_URL = "https://api.unsplash.com/"
    }
}
