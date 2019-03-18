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

    private var mIsMultipleSelection: Boolean = false

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
        picker_search_image_view.setOnClickListener { showSearch() }
        picker_clear_image_view.setOnClickListener { hideSearch(true) }
        picker_done_image_view.setOnClickListener {
            sendImagesAsResult()
        }
        // get the view model and bind search edit text
        mViewModel =
                ViewModelProviders.of(this, Injector.createPickerViewModelFactory()).get(PickerViewModel::class.java)
        observeViewModel()
        mViewModel.bindSearch(picker_edit_text)
        // init the title
        onImageSelected(0, false)
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
        mViewModel.textLiveData.observe(this, Observer {
            picker_clear_image_view.visibility = if (TextUtils.isEmpty(it)) View.GONE else View.VISIBLE
            if (mIsMultipleSelection) {
                mAdapter.clearSelection()
                onImageSelected(0, false)
            }
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

    override fun onImageSelected(nbOfSelectedImages: Int, userInput: Boolean) {
        // if multiple selection
        if (mIsMultipleSelection) {
            // update the title
            picker_title_text_view.text = when (nbOfSelectedImages) {
                0 -> getString(R.string.unsplash)
                1 -> getString(R.string.photo_selected)
                else -> getString(R.string.photos_selected, nbOfSelectedImages)
            }
            //  hide or show the done and search images
            picker_done_image_view.visibility = if (nbOfSelectedImages == 0) View.GONE else View.VISIBLE
            picker_search_image_view.visibility = if (nbOfSelectedImages == 0) View.VISIBLE else View.GONE
            // hide the search if the user selected photo(s)
            if (userInput) {
                hideSearch(false)
            }
        }
        // if single selection send selected photo as a result
        else if (nbOfSelectedImages > 0) {
            sendImagesAsResult()
        }
    }

    /**
     * Shows the search edit text and the clear image.
     */
    private fun showSearch() {
        picker_edit_text.visibility = View.VISIBLE
        picker_clear_image_view.visibility = View.VISIBLE
        picker_edit_text.requestFocus()
        picker_edit_text.openKeyboard(this)
    }

    /**
     * Clears the search criteria if specified
     * and hide the search edit text and the clear image.
     *
     * @param clear true if the search has to be cleared, false otherwise
     */
    private fun hideSearch(clear: Boolean) {
        if (clear && !TextUtils.isEmpty(picker_edit_text.text)) {
            picker_edit_text.setText("")
        }
        picker_edit_text.visibility = View.GONE
        picker_clear_image_view.visibility = View.GONE
        picker_edit_text.closeKeyboard(this)
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
