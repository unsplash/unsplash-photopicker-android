package com.unsplash.pickerandroid.photopicker.domain

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.unsplash.pickerandroid.photopicker.data.NetworkEndpoints
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto

/**
 * Android paging library data source factory.
 * This will create the load photo data source.
 */
class LoadPhotoDataSourceFactory constructor(private val networkEndpoints: NetworkEndpoints) :
    DataSource.Factory<Int, UnsplashPhoto>() {

    val sourceLiveData = MutableLiveData<LoadPhotoDataSource>()

    override fun create(): DataSource<Int, UnsplashPhoto> {
        val source = LoadPhotoDataSource(networkEndpoints)
        sourceLiveData.postValue(source)
        return source
    }
}
