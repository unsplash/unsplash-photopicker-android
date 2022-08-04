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

    override fun getRefreshKey(state: PagingState<Int, UnsplashPhoto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, UnsplashPhoto>> {
        val pageIndex = params.key ?: 1
        return networkEndpoints.loadPhotos(
            UnsplashPhotoPicker.getAccessKey(),
            pageIndex,
            params.loadSize
        ).map { response ->
            if (response.isSuccessful) {

                val items = response.body().orEmpty()

                val nextKey = if (items.isEmpty()) {
                    null
                } else {
                    pageIndex + (params.loadSize / UnsplashPhotoPicker.getPageSize())
                }

                LoadResult.Page(
                    response.body()!!,
                    if (params.key == 1) null else params.key,
                    nextKey
                )
            } else {
                LoadResult.Error(
                    Exception(response.message())
                )
            }
        }.singleOrError().subscribeOn(Schedulers.io())
    }
}