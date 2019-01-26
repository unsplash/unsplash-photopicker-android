package com.unsplash.pickerandroid.photopicker.presentation

import android.text.TextUtils
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.Photo
import com.unsplash.pickerandroid.photopicker.domain.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * View model for the picker screen.
 * This will use the repository to fetch the photos depending on the search criteria.
 * This is using rx binding.
 */
class PickerViewModel constructor(private val repository: Repository) : BaseViewModel() {

    private val mPhotosLiveData = MutableLiveData<PagedList<Photo>>()
    val photosLiveData: LiveData<PagedList<Photo>> get() = mPhotosLiveData

    override fun getTag(): String {
        return PickerViewModel::class.java.simpleName
    }

    /**
     * Binds the edit text using rx binding to listen to text change.
     *
     * @param the edit text to listen to
     */
    fun bindSearch(editText: EditText) {
        RxTextView.textChanges(editText)
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { mLoadingLiveData.postValue(true) }
            .observeOn(Schedulers.io())
            .switchMap { text ->
                if (TextUtils.isEmpty(text)) repository.loadPhotos(UnsplashPhotoPicker.getPageSize())
                else repository.searchPhotos(text.toString(), UnsplashPhotoPicker.getPageSize())
            }
            .subscribe(object : BaseObserver<PagedList<Photo>>() {
                override fun onSuccess(data: PagedList<Photo>?) {
                    mPhotosLiveData.postValue(data)
                }
            })
    }
}
