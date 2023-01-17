package com.unsplash.pickerandroid.photopicker.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Base view model containing the error, message and loading live data while dealing with rx disposables.
 */
abstract class BaseViewModel : ViewModel() {

    // the error live data triggered in case of failure
    protected val mErrorLiveData = MutableLiveData<Boolean>()
    val errorLiveData: LiveData<Boolean> get() = mErrorLiveData

    // the message live data triggered if something has to be shown on the screen
    protected val mMessageLiveData = MutableLiveData<String>()
    val messageLiveData: LiveData<String> get() = mMessageLiveData

    // the loading live data triggered every time the loading state changes
    protected val mLoadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean> get() = mLoadingLiveData

    /**
     * Getting the tag for the logs.
     */
    protected abstract fun getTag(): String
}
