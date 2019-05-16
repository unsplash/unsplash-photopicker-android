package com.unsplash.pickerandroid.photopicker.presentation

import android.text.TextUtils
import android.widget.EditText
import androidx.lifecycle.MediatorLiveData
import androidx.paging.PagedList
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.domain.NetworkState
import com.unsplash.pickerandroid.photopicker.domain.Repository

/**
 * View model for the picker screen.
 * This will use the repository to fetch the photos depending on the search criteria.
 * This is using rx binding.
 */
class UnsplashPickerViewModel constructor(private val repository: Repository) : BaseViewModel() {

    var photosLiveData = MediatorLiveData<PagedList<UnsplashPhoto>>()

    var stateLiveData = MediatorLiveData<NetworkState>()

    private val loadPagedListContainer = repository.loadPhotos(UnsplashPhotoPicker.getPageSize())

    override fun getTag(): String {
        return UnsplashPickerViewModel::class.java.simpleName
    }

    /**
     * Binds the edit text using rx binding to listen to text change.
     *
     * @param editText the edit text to listen to
     */
    fun bindSearch(editText: EditText) {
        if (TextUtils.isEmpty(editText.text)) {
            photosLiveData.addSource(loadPagedListContainer.pagedList) { value ->
                photosLiveData.setValue(value)
            }
            stateLiveData.addSource(loadPagedListContainer.networkState) { value ->
                stateLiveData.setValue(value)
            }
        }
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
