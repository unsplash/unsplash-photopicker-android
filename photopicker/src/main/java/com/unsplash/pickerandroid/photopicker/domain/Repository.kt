package com.unsplash.pickerandroid.photopicker.domain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.NetworkEndpoints
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Simple repository used as a proxy by the view models to fetch data.
 */
class Repository constructor(private val networkEndpoints: NetworkEndpoints) {

    fun loadPhotos(pageSize: Int): PagedListContainer<UnsplashPhoto> {
        val sourceFactory = LoadPhotoDataSourceFactory(networkEndpoints)
        val livePagedList = LivePagedListBuilder(
            sourceFactory,
            PagedList.Config.Builder()
                .setInitialLoadSizeHint(pageSize)
                .setPageSize(pageSize)
                .build()
        ).build()
        return PagedListContainer(
            livePagedList,
            Transformations.switchMap(sourceFactory.sourceLiveData) {
                it.networkState
            })
    }

    fun searchPhotos(criteria: String, pageSize: Int): LiveData<PagedList<UnsplashPhoto>> {
        return LivePagedListBuilder(
            SearchPhotoDataSourceFactory(networkEndpoints, criteria),
            PagedList.Config.Builder()
                .setInitialLoadSizeHint(pageSize)
                .setPageSize(pageSize)
                .build()
        ).build()
    }

    fun trackDownload(url: String?) {
        if (url != null) {
            val authUrl = url + "?client_id=" + UnsplashPhotoPicker.getAccessKey()
            networkEndpoints.trackDownload(authUrl)
                .enqueue(object : Callback<Void> {
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e(Repository::class.java.simpleName, t.message, t)
                    }

                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    }
                })
        }
    }
}
