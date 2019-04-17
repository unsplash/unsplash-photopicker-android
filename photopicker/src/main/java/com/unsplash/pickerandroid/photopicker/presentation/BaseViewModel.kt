package com.unsplash.pickerandroid.photopicker.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

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
     * The rx composite containing the disposables.
     */
    protected val mCompositeDisposable = CompositeDisposable()

    override fun onCleared() {
        mCompositeDisposable.clear()
        super.onCleared()
    }

    /**
     * Getting the tag for the logs.
     */
    protected abstract fun getTag(): String

    protected abstract inner class BaseObserver<Data> : Observer<Data> {
        override fun onComplete() {
        }

        override fun onSubscribe(d: Disposable?) {
            mCompositeDisposable.add(d)
        }

        override fun onNext(value: Data?) {
            if (UnsplashPhotoPicker.isLoggingEnabled()) {
                Log.i(getTag(), value.toString())
            }
            // hiding the loading
            mLoadingLiveData.postValue(false)
            // success
            onSuccess(value)
        }

        override fun onError(e: Throwable?) {
            Log.e(getTag(), e?.message, e)
            // hiding the loading
            mLoadingLiveData.postValue(false)
            // posting the error
            mErrorLiveData.postValue(false)
        }

        abstract fun onSuccess(data: Data?)
    }
}
