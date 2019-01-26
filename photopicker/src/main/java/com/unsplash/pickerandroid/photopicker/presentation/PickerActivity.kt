package com.unsplash.pickerandroid.photopicker.presentation

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        // recycler view layout manager
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        // recycler view adapter
        mAdapter = PhotoAdapter(this, intent.getBooleanExtra(EXTRA_IS_MULTIPLE, false))
        mAdapter.setOnImageSelectedListener(this)
        // recycler view configuration
        picker_recycler_view.setHasFixedSize(true)
        picker_recycler_view.layoutManager = mLayoutManager
        picker_recycler_view.adapter = mAdapter
        // click listeners
        picker_back_image_view.setOnClickListener { onBackPressed() }
        picker_clear_image_view.setOnClickListener { picker_edit_text.setText("") }
        picker_done_image_view.setOnClickListener {
            // TODO send selected photos as a result
        }
        // get the view model and bind search edit text
        mViewModel =
                ViewModelProviders.of(this, Injector.createPickerViewModelFactory()).get(PickerViewModel::class.java)
        observeViewModel()
        mViewModel.bindSearch(picker_edit_text)
        // init the title
        onImageSelected(0)
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
        // TODO
        // if single selection send selected photo as a result
        // if multiple selection update the title and show the done image
    }

    override fun onImageLongPress(imageView: ImageView, url: String) {
        // TODO show the image preview
    }

    companion object {
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
