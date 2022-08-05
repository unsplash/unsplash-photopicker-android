package com.unsplash.pickerandroid.photopicker.domain

import android.net.Uri
import android.util.Log
import androidx.paging.*
import androidx.paging.rxjava2.RxPagingSource
import androidx.paging.rxjava2.flowable
import androidx.paging.rxjava2.observable
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.NetworkEndpoints
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

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
        coroutineScope.launch {
            url.filterNotNull()
                .map {
                    Uri.parse(it).buildUpon()
                        .appendQueryParameter("client_id", UnsplashPhotoPicker.getAccessKey())
                        .build()
                        .toString()
                }
                .forEach {
                    networkEndpoints.trackDownload(it)
                    println("Tracked download of $it")
                }
        }
    }
}
