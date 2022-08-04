package com.unsplash.pickerandroid.photopicker.domain

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.NetworkEndpoints
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.data.SearchResponse
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.internal.notifyAll
import retrofit2.Response

/**
 * Android paging library data source.
 * This will load the photos for the search and allow an infinite scroll on the picker screen.
 */
class SearchPhotoDataSource(
    private val networkEndpoints: NetworkEndpoints,
    private val criteria: String
) : PagingSource<Int, UnsplashPhoto>() {

    val networkState = MutableLiveData<NetworkState>()

    private var lastPage: Int? = null

    override fun getRefreshKey(state: PagingState<Int, UnsplashPhoto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UnsplashPhoto> {
        val pageIndex = params.key ?: 1
        val response =  networkEndpoints.searchPhotos(
            UnsplashPhotoPicker.getAccessKey(),
            criteria,
            pageIndex,
            params.loadSize
        )

        return if (response.isSuccessful) {
            networkState.postValue(NetworkState.SUCCESS)

            val items = response.body()?.results.orEmpty()

            val nextKey = if (items.isEmpty()) {
                null
            } else {
                pageIndex + (params.loadSize / UnsplashPhotoPicker.getPageSize())
            }

            LoadResult.Page(
                items,
                if (pageIndex == 1) null else pageIndex,
                nextKey
            )
        } else {
            LoadResult.Error(
                Exception(response.message())
            )
        }
    }
}
