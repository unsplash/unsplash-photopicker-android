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

    fun trackDownload(url: String?) {
        if (url != null) {
            val uriBuilder = Uri.parse(url).buildUpon()
            uriBuilder.appendQueryParameter("client_id", UnsplashPhotoPicker.getAccessKey())
            val downloadUrl = uriBuilder.build().toString()
            networkEndpoints.trackDownload(downloadUrl)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .doOnError { e ->
                    Log.e(Repository::class.java.simpleName, e?.message, e)
                }
                .subscribe()
        }
    }
}
