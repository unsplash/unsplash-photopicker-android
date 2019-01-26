package com.unsplash.pickerandroid.photopicker.domain

import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.unsplash.pickerandroid.photopicker.data.NetworkEndpoints
import com.unsplash.pickerandroid.photopicker.data.Photo
import io.reactivex.Observable

/**
 * Simple repository used as a proxy by the view models to fetch data.
 */
class Repository constructor(private val networkEndpoints: NetworkEndpoints) {

    fun loadPhotos(pageSize: Int): Observable<PagedList<Photo>> {
        return RxPagedListBuilder(
            LoadPhotoDataSourceFactory(networkEndpoints),
            PagedList.Config.Builder()
                .setInitialLoadSizeHint(pageSize)
                .build()
        ).buildObservable()
    }

    fun searchPhotos(criteria: String, pageSize: Int): Observable<PagedList<Photo>> {
        return RxPagedListBuilder(
            SearchPhotoDataSourceFactory(networkEndpoints, criteria),
            PagedList.Config.Builder()
                .setInitialLoadSizeHint(pageSize)
                .build()
        ).buildObservable()
    }
}
