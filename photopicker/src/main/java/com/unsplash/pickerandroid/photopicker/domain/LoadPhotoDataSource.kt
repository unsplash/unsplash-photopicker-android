package com.unsplash.pickerandroid.photopicker.domain

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.NetworkEndpoints
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Android paging library data source.
 * This will load the photos and allow an infinite scroll on the picker screen.
 */
class LoadPhotoDataSource(private val networkEndpoints: NetworkEndpoints) :
    RxPagingSource<Int, UnsplashPhoto>() {

    private var lastPage: Int? = null

    override fun getRefreshKey(state: PagingState<Int, UnsplashPhoto>): Int? {
        return state.anchorPosition
    }

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, UnsplashPhoto>> {
        return networkEndpoints.loadPhotos(
            UnsplashPhotoPicker.getAccessKey(),
            params.key ?: 0,
            params.loadSize
        ).map { response ->
            if (response.isSuccessful) {
                val nextPage = if (params.key == lastPage) null else params.key!! + 1
                LoadResult.Page(
                    response.body()!!,
                    params.key,
                    nextPage
                )
            } else {
                LoadResult.Error(
                    Exception(response.message())
                )
            }
        }.singleOrError()
    }
}