package com.unsplash.pickerandroid.photopicker.domain

import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.NetworkEndpoints
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Simple repository used as a proxy by the view models to fetch data.
 */
class Repository constructor(private val networkEndpoints: NetworkEndpoints) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

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

    fun trackDownload(vararg url: String?) {
        // Required by api when doing a download, it uses its own coroutineScope to avoid being
        // cancelled from outside coroutine cancelation
        coroutineScope.launch {
            url.filterNotNull()
                .map {
                    Uri.parse(it).buildUpon()
                        .appendQueryParameter("client_id", UnsplashPhotoPicker.getAccessKey())
                        .build()
                        .toString()
                }
                .forEach {
                    runCatching { networkEndpoints.trackDownload(it) }
                        .onFailure { Log.e("Repository", it.message, it) }
                }
        }
    }
}
