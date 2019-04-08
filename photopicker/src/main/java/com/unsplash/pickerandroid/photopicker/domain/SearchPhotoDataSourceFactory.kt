package com.unsplash.pickerandroid.photopicker.domain

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.unsplash.pickerandroid.photopicker.data.NetworkEndpoints
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto

/**
 * Android paging library data source factory.
 * This will create the search photo data source.
 */
class SearchPhotoDataSourceFactory constructor(
    private val networkEndpoints: NetworkEndpoints,
    private val criteria: String
) :
    DataSource.Factory<Int, UnsplashPhoto>() {

    val sourceLiveData = MutableLiveData<SearchPhotoDataSource>()

    override fun create(): DataSource<Int, UnsplashPhoto> {
        val source = SearchPhotoDataSource(networkEndpoints, criteria)
        sourceLiveData.postValue(source)
        return source
    }
}
