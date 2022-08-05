package com.unsplash.pickerandroid.photopicker.domain

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.NetworkEndpoints
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto

/**
 * Android paging library data source.
 * This will load the photos and allow an infinite scroll on the picker screen.
 */
class LoadPhotoDataSource(private val networkEndpoints: NetworkEndpoints) :
    PagingSource<Int, UnsplashPhoto>() {

    override fun getRefreshKey(state: PagingState<Int, UnsplashPhoto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UnsplashPhoto> {
        val pageIndex = params.key ?: 1

        val response = networkEndpoints.loadPhotos(
            UnsplashPhotoPicker.getAccessKey(), pageIndex, params.loadSize
        )

        return if (response.isSuccessful) {
            val items = response.body().orEmpty()

            val nextKey = if (items.isEmpty()) {
                null
            } else {
                pageIndex + (params.loadSize / UnsplashPhotoPicker.getPageSize())
            }

            LoadResult.Page(
                response.body()!!, if (params.key == 1) null else params.key, nextKey
            )
        } else {
            LoadResult.Error(
                Exception(response.message())
            )
        }
    }
}