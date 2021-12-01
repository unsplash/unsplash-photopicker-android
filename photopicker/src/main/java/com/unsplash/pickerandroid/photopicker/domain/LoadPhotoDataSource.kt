package com.unsplash.pickerandroid.photopicker.domain

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.NetworkEndpoints
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import retrofit2.Response

/**
 * Android paging library data source.
 * This will load the photos and allow an infinite scroll on the picker screen.
 */
class LoadPhotoDataSource(private val networkEndpoints: NetworkEndpoints) : PageKeyedDataSource<Int, UnsplashPhoto>() {

    val networkState = MutableLiveData<NetworkState>()

    private var lastPage: Int? = null

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, UnsplashPhoto>) {
        // updating the network state to loading
        networkState.postValue(NetworkState.LOADING)
        // api call for the first page
        networkEndpoints.loadPhotos(UnsplashPhotoPicker.getAccessKey(), 1, params.requestedLoadSize)
            .subscribe(object : Observer<Response<List<UnsplashPhoto>>> {
                override fun onComplete() {
                    // do nothing on this terminal event
                }

                override fun onSubscribe(d: Disposable) {
                    // we don't keep the disposable
                }

                override fun onNext(response: Response<List<UnsplashPhoto>>) {
                    // if the response is successful
                    // we get the last page number
                    // we push the result on the paging callback
                    // we update the network state to success
                    if (response.isSuccessful) {
                        lastPage = response.headers().get("x-total")?.toInt()?.div(params.requestedLoadSize)
                        callback.onResult(response.body()!!, null, 2)
                        networkState.postValue(NetworkState.SUCCESS)
                    }
                    // if the response is not successful
                    // we update the network state to error along with the error message
                    else {
                        networkState.postValue(NetworkState.error(response.message()))
                    }
                }

                override fun onError(e: Throwable) {
                    // we update the network state to error along with the error message
                    networkState.postValue(NetworkState.error(e.message))
                }
            })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, UnsplashPhoto>) {
        // updating the network state to loading
        networkState.postValue(NetworkState.LOADING)
        // api call for the subsequent pages
        networkEndpoints.loadPhotos(UnsplashPhotoPicker.getAccessKey(), params.key, params.requestedLoadSize)
            .subscribe(object : Observer<Response<List<UnsplashPhoto>>> {
                override fun onComplete() {
                    // do nothing on this terminal event
                }

                override fun onSubscribe(d: Disposable) {
                    // we don't keep the disposable
                }

                override fun onNext(response: Response<List<UnsplashPhoto>>) {
                    // if the response is successful
                    // we get the next page number
                    // we push the result on the paging callback
                    // we update the network state to success
                    if (response.isSuccessful) {
                        val nextPage = if (params.key == lastPage) null else params.key + 1
                        callback.onResult(response.body()!!, nextPage)
                        networkState.postValue(NetworkState.SUCCESS)
                    }
                    // if the response is not successful
                    // we update the network state to error along with the error message
                    else {
                        networkState.postValue(NetworkState.error(response.message()))
                    }
                }

                override fun onError(e: Throwable) {
                    // we update the network state to error along with the error message
                    networkState.postValue(NetworkState.error(e.message))
                }
            })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, UnsplashPhoto>) {
        // we do nothing here because everything will be loaded
    }
}
