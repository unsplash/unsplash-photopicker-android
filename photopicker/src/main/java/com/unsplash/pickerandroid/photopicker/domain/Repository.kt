package com.unsplash.pickerandroid.photopicker.domain

import android.util.Log
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
 * Will use the paging library.
 */
class Repository constructor(private val networkEndpoints: NetworkEndpoints) {

    /**
     * Loads the photos using the paging library.
     *
     * @param pageSize the number of elements per page
     * @return a paged list container hosting the paged list live data and the network state live data
     */
    fun loadPhotos(pageSize: Int): PagedListContainer<UnsplashPhoto> {
        // getting the load data source factory
        val sourceFactory = LoadPhotoDataSourceFactory(networkEndpoints)
        // building the paged list live data for loading photos
        val livePagedList = LivePagedListBuilder(
            sourceFactory,
            PagedList.Config.Builder()
                .setInitialLoadSizeHint(pageSize)
                .setPageSize(pageSize)
                .build()
        ).build()
        // returning the container
        return PagedListContainer(
            livePagedList,
            Transformations.switchMap(sourceFactory.sourceLiveData) {
                it.networkState
            })
    }

    /**
     * Search for photos using the paging library.
     *
     * @param criteria the search criteria
     * @param pageSize the number of elements per page
     * @return a paged list container hosting the paged list live data and the network state live data
     */
    fun searchPhotos(criteria: String, pageSize: Int): PagedListContainer<UnsplashPhoto> {
        // getting the search data source factory
        val sourceFactory = SearchPhotoDataSourceFactory(networkEndpoints, criteria)
        // building the paged list live data for searching for photos
        val livePagedList = LivePagedListBuilder(
            sourceFactory,
            PagedList.Config.Builder()
                .setInitialLoadSizeHint(pageSize)
                .setPageSize(pageSize)
                .build()
        ).build()
        // returning the container
        return PagedListContainer(
            livePagedList,
            Transformations.switchMap(sourceFactory.sourceLiveData) {
                it.networkState
            })
    }

    /**
     * Tracking the photo selection.
     *
     * @param url the download url
     */
    fun trackDownload(url: String?) {
        url?.run {
            // building the url with the auth
            val authUrl = url + "?client_id=" + UnsplashPhotoPicker.getAccessKey()
            // making the api call
            networkEndpoints.trackDownload(authUrl)
                .enqueue(object : Callback<Void> {
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e(Repository::class.java.simpleName, t.message, t)
                    }

                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        // doing nothing
                    }
                })
        }
    }
}
