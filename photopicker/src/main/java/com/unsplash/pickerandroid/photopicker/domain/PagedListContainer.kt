package com.unsplash.pickerandroid.photopicker.domain

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

data class PagedListContainer<T>(
    val pagedList: LiveData<PagedList<T>>,
    val networkState: LiveData<NetworkState>
)
