package com.unsplash.pickerandroid.photopicker.presentation

import android.text.TextUtils
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.paging.PagingData
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.domain.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * View model for the picker screen.
 * This will use the repository to fetch the photos depending on the search criteria.
 * This is using rx binding.
 */
class UnsplashPickerViewModel constructor(private val repository: Repository) : BaseViewModel() {

    private val mPhotosLiveData = MutableLiveData<PagingData<UnsplashPhoto>>()
    val photosLiveData: LiveData<PagingData<UnsplashPhoto>> get() = mPhotosLiveData

    override fun getTag(): String {
        return UnsplashPickerViewModel::class.java.simpleName
    }

    /**
     * Binds the edit text using rx binding to listen to text change.
     *
     * @param editText the edit text to listen to
     */
    fun bindSearch(editText: EditText) {
        RxTextView.textChanges(editText)
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                mLoadingLiveData.postValue(true)
            }
            .observeOn(Schedulers.io())
            .switchMap { text ->
                if (TextUtils.isEmpty(text)) repository.loadPhotos(UnsplashPhotoPicker.getPageSize())
                else repository.searchPhotos(text.toString(), UnsplashPhotoPicker.getPageSize())
            }
            .subscribeOn(Schedulers.io())
            .subscribe(
                object : BaseObserver<PagingData<UnsplashPhoto>>() {
                    override fun onSuccess(data: PagingData<UnsplashPhoto>) {
                        mPhotosLiveData.postValue(data)
                    }
                }
            )
    }

    /**
     * To abide by the API guidelines,
     * you need to trigger a GET request to this endpoint every time your application performs a download of a photo
     *
     * @param photos the list of selected photos
     */
    fun trackDownloads(photos: ArrayList<UnsplashPhoto>) {
        for (photo in photos) {
            repository.trackDownload(photo.links.download_location)
        }
    }
}
