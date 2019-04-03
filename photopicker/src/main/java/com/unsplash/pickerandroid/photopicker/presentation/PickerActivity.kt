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
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.unsplash.pickerandroid.photopicker.Injector
import com.unsplash.pickerandroid.photopicker.R
import kotlinx.android.synthetic.main.activity_picker.*

/**
 * Main screen for the picker.
 * This will show a list a photos and a search component.
 * The list is has an infinite scroll.
 */
class PickerActivity : AppCompatActivity(), OnImageSelectedListener {

    private lateinit var mLayoutManager: StaggeredGridLayoutManager

    private lateinit var mAdapter: PhotoAdapter

    private lateinit var mViewModel: PickerViewModel

    private var mIsMultipleSelection = false

    private var mCurrentState = PickerState.IDLE

    private var mPreviousState = PickerState.IDLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        mIsMultipleSelection = intent.getBooleanExtra(EXTRA_IS_MULTIPLE, false)
        // recycler view layout manager
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        // recycler view adapter
        mAdapter = PhotoAdapter(this, mIsMultipleSelection)
        mAdapter.setOnImageSelectedListener(this)
        // recycler view configuration
        picker_recycler_view.setHasFixedSize(true)
        picker_recycler_view.itemAnimator = null
        picker_recycler_view.layoutManager = mLayoutManager
        picker_recycler_view.adapter = mAdapter
        // click listeners
        picker_back_image_view.setOnClickListener { onBackPressed() }
        picker_cancel_image_view.setOnClickListener { onBackPressed() }
        picker_clear_image_view.setOnClickListener { onBackPressed() }
        picker_search_image_view.setOnClickListener {
            // updating state
            mCurrentState = PickerState.SEARCHING
            updateUiFromState()
        }
        picker_done_image_view.setOnClickListener { sendImagesAsResult() }
        // get the view model and bind search edit text
        mViewModel =
                ViewModelProviders.of(this, Injector.createPickerViewModelFactory()).get(PickerViewModel::class.java)
        observeViewModel()
        mViewModel.bindSearch(picker_edit_text)
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
            picker_progress_bar_layout.visibility = if (it != null && it) View.VISIBLE else View.GONE
        })
        mViewModel.photosLiveData.observe(this, Observer {
            picker_no_result_text_view.visibility =
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

    override fun onImageSelected(nbOfSelectedImages: Int) {
        // if multiple selection
        if (mIsMultipleSelection) {
            // update the title
            picker_title_text_view.text = when (nbOfSelectedImages) {
                0 -> getString(R.string.unsplash)
                1 -> getString(R.string.photo_selected)
                else -> getString(R.string.photos_selected, nbOfSelectedImages)
            }
            // updating state
            if (nbOfSelectedImages > 0) {
                // only once, ignoring all subsequent photo selections
                if (mCurrentState != PickerState.PHOTO_SELECTED) {
                    mPreviousState = mCurrentState
                    mCurrentState = PickerState.PHOTO_SELECTED
                }
                updateUiFromState()
            } else { // no photo selected means un-selection
                onBackPressed()
            }
        }
        // if single selection send selected photo as a result
        else if (nbOfSelectedImages > 0) {
            sendImagesAsResult()
        }
    }

    /**
     * Sends images in the result intent as a result for the calling activity.
     */
    private fun sendImagesAsResult() {
        val images: ArrayList<Image> = mAdapter.getImages()
        val data = Intent()
        data.putExtra(EXTRA_IMAGES, images)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onImageLongPress(imageView: ImageView, url: String) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageView as View, "image")
        startActivity(ImageShowActivity.getStartingIntent(this, url), options.toBundle())
    }

    override fun onBackPressed() {
        when (mCurrentState) {
            PickerState.IDLE -> {
                super.onBackPressed()
            }
            PickerState.SEARCHING -> {
                // updating states
                mCurrentState = PickerState.IDLE
                mPreviousState = PickerState.SEARCHING
                // updating ui
                updateUiFromState()
            }
            PickerState.PHOTO_SELECTED -> {
                // updating states
                mCurrentState = if (mPreviousState == PickerState.SEARCHING) {
                    PickerState.SEARCHING
                } else {
                    PickerState.IDLE
                }
                mPreviousState = PickerState.PHOTO_SELECTED
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
            PickerState.IDLE -> {
                // back and search buttons visible
                picker_back_image_view.visibility = View.VISIBLE
                picker_search_image_view.visibility = View.VISIBLE
                // cancel and done buttons gone
                picker_cancel_image_view.visibility = View.GONE
                picker_done_image_view.visibility = View.GONE
                // edit text cleared and gone
                if (!TextUtils.isEmpty(picker_edit_text.text)) {
                    picker_edit_text.setText("")
                }
                picker_edit_text.visibility = View.GONE
                // right clear button on top of edit text gone
                picker_clear_image_view.visibility = View.GONE
                // keyboard down
                picker_edit_text.closeKeyboard(this)
                // action bar with unsplash
                picker_title_text_view.text = getString(R.string.unsplash)
                // clear list selection
                mAdapter.clearSelection()
                mAdapter.notifyDataSetChanged()
            }
            PickerState.SEARCHING -> {
                // back, cancel, done or search buttons gone
                picker_back_image_view.visibility = View.GONE
                picker_cancel_image_view.visibility = View.GONE
                picker_done_image_view.visibility = View.GONE
                picker_search_image_view.visibility = View.GONE
                // edit text visible and focused
                picker_edit_text.visibility = View.VISIBLE
                // right clear button on top of edit text visible
                picker_clear_image_view.visibility = View.VISIBLE
                // keyboard up
                picker_edit_text.requestFocus()
                picker_edit_text.openKeyboard(this)
                // clear list selection
                mAdapter.clearSelection()
                mAdapter.notifyDataSetChanged()
            }
            PickerState.PHOTO_SELECTED -> {
                // back and search buttons gone
                picker_back_image_view.visibility = View.GONE
                picker_search_image_view.visibility = View.GONE
                // cancel and done buttons visible
                picker_cancel_image_view.visibility = View.VISIBLE
                picker_done_image_view.visibility = View.VISIBLE
                // edit text gone
                picker_edit_text.visibility = View.GONE
                // right clear button on top of edit text gone
                picker_clear_image_view.visibility = View.GONE
                // keyboard down
                picker_edit_text.closeKeyboard(this)
            }
        }
    }

    companion object {
        const val EXTRA_IMAGES = "EXTRA_IMAGES"
        private const val EXTRA_IS_MULTIPLE = "EXTRA_IS_MULTIPLE"

        /**
         * @param callingContext the calling context
         * @param isMultipleSelection true if multiple selection, false otherwise
         *
         * @return the intent needed to come to this activity
         */
        fun getStartingIntent(callingContext: Context, isMultipleSelection: Boolean): Intent {
            val intent = Intent(callingContext, PickerActivity::class.java)
            intent.putExtra(EXTRA_IS_MULTIPLE, isMultipleSelection)
            return intent
        }
    }
}
