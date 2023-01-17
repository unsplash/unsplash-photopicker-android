package com.unsplash.pickerandroid.photopicker.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.unsplash.pickerandroid.photopicker.Injector
import com.unsplash.pickerandroid.photopicker.R
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.databinding.ActivityPickerBinding

/**
 * Main screen for the picker.
 * This will show a list a photos and a search component.
 * The list is has an infinite scroll.
 */
class UnsplashPickerActivity : AppCompatActivity(), OnPhotoSelectedListener {

    private lateinit var mAdapter: UnsplashPhotoAdapter

    private val mViewModel: UnsplashPickerViewModel by viewModels {
        Injector.createPickerViewModelFactory()
    }

    private var mIsMultipleSelection = false

    private var mCurrentState = UnsplashPickerState.IDLE

    private var mPreviousState = UnsplashPickerState.IDLE

    private lateinit var binding: ActivityPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mIsMultipleSelection = intent.getBooleanExtra(EXTRA_IS_MULTIPLE, false)

        // recycler view adapter
        mAdapter = UnsplashPhotoAdapter(mIsMultipleSelection)
        mAdapter.setOnImageSelectedListener(this)
        mAdapter.addLoadStateListener { loadState ->
            if (loadState.append.endOfPaginationReached) {
                binding.unsplashPickerNoResultTextView.isVisible = mAdapter.itemCount < 1
            }
        }

        // recycler view configuration
        binding.unsplashPickerRecyclerView.apply {
            setHasFixedSize(true)
            itemAnimator = null
            adapter = mAdapter
        }

        onBackPressedDispatcher.addCallback {
            when (mCurrentState) {
                UnsplashPickerState.IDLE -> {
                    finish()
                }
                UnsplashPickerState.SEARCHING -> {
                    // updating states
                    mCurrentState = UnsplashPickerState.IDLE
                    mPreviousState = UnsplashPickerState.SEARCHING
                    // updating ui
                    updateUiFromState()
                }
                UnsplashPickerState.PHOTO_SELECTED -> {
                    // updating states
                    mCurrentState = if (mPreviousState == UnsplashPickerState.SEARCHING) {
                        UnsplashPickerState.SEARCHING
                    } else {
                        UnsplashPickerState.IDLE
                    }
                    mPreviousState = UnsplashPickerState.PHOTO_SELECTED
                    // updating ui
                    updateUiFromState()
                }
            }
        }

        // click listeners
        binding.unsplashPickerBackImageView.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.unsplashPickerCancelImageView.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.unsplashPickerClearImageView.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.unsplashPickerSearchImageView.setOnClickListener {
            // updating state
            mCurrentState = UnsplashPickerState.SEARCHING
            updateUiFromState()
        }
        binding.unsplashPickerDoneImageView.setOnClickListener { sendPhotosAsResult() }

        binding.unsplashPickerEditText.doOnTextChanged { text, _, _, _ ->
            mViewModel.onQueryChanged(text.toString())
        }

        observeViewModel()
    }

    /**
     * Observes the live data in the view model.
     */
    private fun observeViewModel() {
        mViewModel.errorLiveData.observe(this) {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
        }
        mViewModel.messageLiveData.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        mViewModel.loadingLiveData.observe(this) {
            binding.unsplashPickerProgressBarLayout.isVisible = it != null && it
        }
        mViewModel.photosLiveData.observe(this) {
            mAdapter.submitData(lifecycle, it)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // we want the recycler view to have 3 columns when in landscape and 2 in portrait
        val layoutManager = binding.unsplashPickerRecyclerView.layoutManager as GridLayoutManager
        layoutManager.spanCount = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            3
        else
            2
        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount)
    }

    override fun onPhotoSelected(nbOfSelectedPhotos: Int) {
        // if multiple selection
        if (mIsMultipleSelection) {
            // update the title
            binding.unsplashPickerTitleTextView.text = when (nbOfSelectedPhotos) {
                0 -> getString(R.string.unsplash)
                1 -> getString(R.string.photo_selected)
                else -> getString(R.string.photos_selected, nbOfSelectedPhotos)
            }
            // updating state
            if (nbOfSelectedPhotos > 0) {
                // only once, ignoring all subsequent photo selections
                if (mCurrentState != UnsplashPickerState.PHOTO_SELECTED) {
                    mPreviousState = mCurrentState
                    mCurrentState = UnsplashPickerState.PHOTO_SELECTED
                }
                updateUiFromState()
            } else { // no photo selected means un-selection
                onBackPressedDispatcher.onBackPressed()
            }
        }
        // if single selection send selected photo as a result
        else if (nbOfSelectedPhotos > 0) {
            sendPhotosAsResult()
        }
    }

    /**
     * Sends images in the result intent as a result for the calling activity.
     */
    private fun sendPhotosAsResult() {
        // get the selected photos
        val photos: ArrayList<UnsplashPhoto> = mAdapter.getImages()
        // track the downloads
        mViewModel.trackDownloads(photos)
        // send them back to the calling activity
        val data = Intent()
        data.putExtra(EXTRA_PHOTOS, photos)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onPhotoLongPress(imageView: ImageView, url: String) {
        startActivity(PhotoShowActivity.getStartingIntent(this, url))
    }

    /*
    STATES
     */

    private fun updateUiFromState() {
        when (mCurrentState) {
            UnsplashPickerState.IDLE -> {
                // back and search buttons visible
                binding.unsplashPickerBackImageView.isVisible = true
                binding.unsplashPickerSearchImageView.isVisible = true

                // cancel and done buttons gone
                binding.unsplashPickerCancelImageView.isVisible = false
                binding.unsplashPickerDoneImageView.isVisible = false

                // edit text cleared and gone
                if (!TextUtils.isEmpty(binding.unsplashPickerEditText.text)) {
                    binding.unsplashPickerEditText.setText("")
                }
                binding.unsplashPickerEditText.isInvisible = false

                // right clear button on top of edit text gone
                binding.unsplashPickerClearImageView.isInvisible = false

                // keyboard down
                binding.unsplashPickerEditText.closeKeyboard(this)

                // action bar with unsplash
                binding.unsplashPickerTitleTextView.text = getString(R.string.unsplash)

                // clear list selection
                mAdapter.clearSelection()
                mAdapter.notifyDataSetChanged()
            }
            UnsplashPickerState.SEARCHING -> {
                // back, cancel, done or search buttons gone
                binding.unsplashPickerBackImageView.isInvisible = false
                binding.unsplashPickerCancelImageView.isInvisible = false
                binding.unsplashPickerDoneImageView.isInvisible = false
                binding.unsplashPickerSearchImageView.isInvisible = false

                // edit text visible and focused
                binding.unsplashPickerEditText.isVisible = true

                // right clear button on top of edit text visible
                binding.unsplashPickerClearImageView.isVisible = true

                // keyboard up
                binding.unsplashPickerEditText.requestFocus()
                binding.unsplashPickerEditText.openKeyboard(this)

                // clear list selection
                mAdapter.clearSelection()
                mAdapter.notifyDataSetChanged()
            }
            UnsplashPickerState.PHOTO_SELECTED -> {
                // back and search buttons gone
                binding.unsplashPickerBackImageView.isVisible = false
                binding.unsplashPickerSearchImageView.isVisible = false

                // cancel and done buttons visible
                binding.unsplashPickerCancelImageView.isVisible = true
                binding.unsplashPickerDoneImageView.isVisible = true

                // edit text gone
                binding.unsplashPickerEditText.isVisible = false

                // right clear button on top of edit text gone
                binding.unsplashPickerClearImageView.isVisible = false

                // keyboard down
                binding.unsplashPickerEditText.closeKeyboard(this)
            }
        }
    }

    companion object {
        const val EXTRA_PHOTOS = "EXTRA_PHOTOS"
        private const val EXTRA_IS_MULTIPLE = "EXTRA_IS_MULTIPLE"

        /**
         * @param callingContext the calling context
         * @param isMultipleSelection true if multiple selection, false otherwise
         *
         * @return the intent needed to come to this activity
         */
        fun getStartingIntent(callingContext: Context, isMultipleSelection: Boolean): Intent {
            val intent = Intent(callingContext, UnsplashPickerActivity::class.java)
            intent.putExtra(EXTRA_IS_MULTIPLE, isMultipleSelection)
            return intent
        }
    }
}
