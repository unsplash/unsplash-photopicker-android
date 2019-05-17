package com.unsplash.pickerandroid.photopicker.presentation

import android.text.TextUtils
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.domain.NetworkState
import com.unsplash.pickerandroid.photopicker.domain.Repository

/**
 * View model for the picker screen.
 * This will use the repository to fetch the photos depending on the search criteria.
 */
class UnsplashPickerViewModel constructor(private val repository: Repository) : ViewModel() {

    /**
     * The photos live data the activity is observing.
     * It is a mediator live data because it needs to be able to change its source.
     */
    var photosLiveData = MediatorLiveData<PagedList<UnsplashPhoto>>()

    /**
     * The network state live data the activity is observing.
     * It is a mediator live data because it needs to be able to change its source.
     */
    var stateLiveData = MediatorLiveData<NetworkState>()

    /**
     * The load paged list container hosting the load photos live data and its network state live data.
     */
    private var loadPagedListContainer = repository.loadPhotos(UnsplashPhotoPicker.getPageSize())

    /**
     * The search paged list container hosting the search photos live data and its network state live data.
     */
    private var searchPagedListContainer = repository.searchPhotos("", UnsplashPhotoPicker.getPageSize())

    /**
     * Load photos depending on the search criteria.
     *
     * @param criteria the edit text to listen to
     */
    fun load(criteria: String?) {
        if (TextUtils.isEmpty(criteria)) {
            loadPhotos()
        } else {
            searchPhotos(criteria!!) // thanks to TextUtils, we know it cannot be null
        }
    }

    /**
     * Will load photos.
     */
    private fun loadPhotos() {
        // removing old sources
        photosLiveData.removeSource(searchPagedListContainer.pagedList)
        stateLiveData.removeSource(searchPagedListContainer.networkState)
        // loading photos
        loadPagedListContainer = repository.loadPhotos(UnsplashPhotoPicker.getPageSize())
        // adding new sources
        photosLiveData.addSource(loadPagedListContainer.pagedList) { value ->
            photosLiveData.setValue(value)
        }
        stateLiveData.addSource(loadPagedListContainer.networkState) { value ->
            stateLiveData.setValue(value)
        }
    }

    /**
     * Will search for photos based on the search criteria.
     *
     * @param criteria the search criteria
     */
    private fun searchPhotos(criteria: String) {
        // removing old sources
        photosLiveData.removeSource(loadPagedListContainer.pagedList)
        stateLiveData.removeSource(loadPagedListContainer.networkState)
        // searching for photos
        searchPagedListContainer = repository.searchPhotos(criteria, UnsplashPhotoPicker.getPageSize())
        // adding new sources
        photosLiveData.addSource(searchPagedListContainer.pagedList) { value ->
            photosLiveData.setValue(value)
        }
        stateLiveData.addSource(searchPagedListContainer.networkState) { value ->
            stateLiveData.setValue(value)
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
