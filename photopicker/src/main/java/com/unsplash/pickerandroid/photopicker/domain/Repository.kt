package com.unsplash.pickerandroid.photopicker.domain

import android.net.Uri
import android.util.Log
import androidx.paging.*
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.NetworkEndpoints
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import kotlinx.coroutines.flow.Flow

/**
 * Simple repository used as a proxy by the view models to fetch data.
 */
class Repository constructor(private val networkEndpoints: NetworkEndpoints) {

    fun loadPhotos(pageSize: Int): Flow<PagingData<UnsplashPhoto>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize
            ),
            initialKey = null,
            pagingSourceFactory = {
                LoadPhotoDataSource(networkEndpoints)
            }
        ).flow
    }

    fun searchPhotos(criteria: String, pageSize: Int): Flow<PagingData<UnsplashPhoto>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize
            ),
            initialKey = null,
            pagingSourceFactory = {
                SearchPhotoDataSource(networkEndpoints, criteria)
            }
        ).flow
    }

    suspend fun trackDownload(url: String?) {
        if (url != null) {
            val downloadUrl = Uri.parse(url).buildUpon()
                .appendQueryParameter("client_id", UnsplashPhotoPicker.getAccessKey())
                .build()
                .toString()

            runCatching { networkEndpoints.trackDownload(downloadUrl) }
                .onFailure {
                    Log.e(Repository::class.java.simpleName, it.message, it)
                }
        }
    }
}
