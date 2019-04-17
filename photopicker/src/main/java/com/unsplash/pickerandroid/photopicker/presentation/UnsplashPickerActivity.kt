package com.unsplash.pickerandroid.photopicker.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.unsplash.pickerandroid.photopicker.Injector
import com.unsplash.pickerandroid.photopicker.R
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import kotlinx.android.synthetic.main.activity_picker.*

/**
 * Main screen for the picker.
 * This will show a list a photos and a search component.
 * The list is has an infinite scroll.
 */
class UnsplashPickerActivity : AppCompatActivity(), OnPhotoSelectedListener {

    private lateinit var mLayoutManager: StaggeredGridLayoutManager

    private lateinit var mAdapter: UnsplashPhotoAdapter

    private lateinit var mViewModel: UnsplashPickerViewModel

    private var mIsMultipleSelection = false

    private var mCurrentState = UnsplashPickerState.IDLE

    private var mPreviousState = UnsplashPickerState.IDLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        mIsMultipleSelection = intent.getBooleanExtra(EXTRA_IS_MULTIPLE, false)
        // recycler view layout manager
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        // recycler view adapter
        mAdapter = UnsplashPhotoAdapter(this, mIsMultipleSelection)
        mAdapter.setOnImageSelectedListener(this)
        // recycler view configuration
        unsplash_picker_recycler_view.setHasFixedSize(true)
        unsplash_picker_recycler_view.itemAnimator = null
        unsplash_picker_recycler_view.layoutManager = mLayoutManager
        unsplash_picker_recycler_view.adapter = mAdapter
        // click listeners
        unsplash_picker_back_image_view.setOnClickListener { onBackPressed() }
        unsplash_picker_cancel_image_view.setOnClickListener { onBackPressed() }
        unsplash_picker_clear_image_view.setOnClickListener { onBackPressed() }
        unsplash_picker_search_image_view.setOnClickListener {
            // updating state
            mCurrentState = UnsplashPickerState.SEARCHING
            updateUiFromState()
        }
        unsplash_picker_done_image_view.setOnClickListener { sendPhotosAsResult() }
        // get the view model and bind search edit text
        mViewModel =
                ViewModelProviders.of(this, Injector.createPickerViewModelFactory())
                    .get(UnsplashPickerViewModel::class.java)
        observeViewModel()
        mViewModel.bindSearch(unsplash_picker_edit_text)
    }

    /**
     * Observes the live data in the view model.
     */
    private fun observeViewModel() {
        mViewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
        })
        mViewModel.messageLiveData.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
        mViewModel.loadingLiveData.observe(this, Observer {
            unsplash_picker_progress_bar_layout.visibility = if (it != null && it) View.VISIBLE else View.GONE
        })
        mViewModel.photosLiveData.observe(this, Observer {
            unsplash_picker_no_result_text_view.visibility =
                    if (it == null || it.isEmpty()) View.VISIBLE
                    else View.GONE
            mAdapter.submitList(it)
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // we want the recycler view to have 3 columns when in landscape and 2 in portrait
        mLayoutManager.spanCount =
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 3
                else 2
        mAdapter.notifyDataSetChanged()
    }

    override fun onPhotoSelected(nbOfSelectedPhotos: Int) {
        // if multiple selection
        if (mIsMultipleSelection) {
            // update the title
            unsplash_picker_title_text_view.text = when (nbOfSelectedPhotos) {
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
                onBackPressed()
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

    override fun onBackPressed() {
        when (mCurrentState) {
            UnsplashPickerState.IDLE -> {
                super.onBackPressed()
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

    /*
    STATES
     */

    private fun updateUiFromState() {
        when (mCurrentState) {
            UnsplashPickerState.IDLE -> {
                // back and search buttons visible
                unsplash_picker_back_image_view.visibility = View.VISIBLE
                unsplash_picker_search_image_view.visibility = View.VISIBLE
                // cancel and done buttons gone
                unsplash_picker_cancel_image_view.visibility = View.GONE
                unsplash_picker_done_image_view.visibility = View.GONE
                // edit text cleared and gone
                if (!TextUtils.isEmpty(unsplash_picker_edit_text.text)) {
                    unsplash_picker_edit_text.setText("")
                }
                unsplash_picker_edit_text.visibility = View.GONE
                // right clear button on top of edit text gone
                unsplash_picker_clear_image_view.visibility = View.GONE
                // keyboard down
                unsplash_picker_edit_text.closeKeyboard(this)
                // action bar with unsplash
                unsplash_picker_title_text_view.text = getString(R.string.unsplash)
                // clear list selection
                mAdapter.clearSelection()
                mAdapter.notifyDataSetChanged()
            }
            UnsplashPickerState.SEARCHING -> {
                // back, cancel, done or search buttons gone
                unsplash_picker_back_image_view.visibility = View.GONE
                unsplash_picker_cancel_image_view.visibility = View.GONE
                unsplash_picker_done_image_view.visibility = View.GONE
                unsplash_picker_search_image_view.visibility = View.GONE
                // edit text visible and focused
                unsplash_picker_edit_text.visibility = View.VISIBLE
                // right clear button on top of edit text visible
                unsplash_picker_clear_image_view.visibility = View.VISIBLE
                // keyboard up
                unsplash_picker_edit_text.requestFocus()
                unsplash_picker_edit_text.openKeyboard(this)
                // clear list selection
                mAdapter.clearSelection()
                mAdapter.notifyDataSetChanged()
            }
            UnsplashPickerState.PHOTO_SELECTED -> {
                // back and search buttons gone
                unsplash_picker_back_image_view.visibility = View.GONE
                unsplash_picker_search_image_view.visibility = View.GONE
                // cancel and done buttons visible
                unsplash_picker_cancel_image_view.visibility = View.VISIBLE
                unsplash_picker_done_image_view.visibility = View.VISIBLE
                // edit text gone
                unsplash_picker_edit_text.visibility = View.GONE
                // right clear button on top of edit text gone
                unsplash_picker_clear_image_view.visibility = View.GONE
                // keyboard down
                unsplash_picker_edit_text.closeKeyboard(this)
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
