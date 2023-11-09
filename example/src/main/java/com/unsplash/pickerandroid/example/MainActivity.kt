package com.unsplash.pickerandroid.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.presentation.UnsplashPickerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mAdapter: PhotoAdapter

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.main_recycler_view) }
    private val pickButton: Button by lazy { findViewById(R.id.main_pick_button) }
    private val singleRadioButton: RadioButton by lazy { findViewById(R.id.main_single_radio_button) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // result adapter
        // recycler view configuration
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        mAdapter = PhotoAdapter(this)
        recyclerView.adapter = mAdapter
        // on the pick button click, we start the library picker activity
        // we are expecting a result from it so we start it for result
        pickButton.setOnClickListener {
            startActivityForResult(
                UnsplashPickerActivity.getStartingIntent(
                    this,
                    !singleRadioButton.isChecked
                ), REQUEST_CODE
            )
        }
    }

    // here we are receiving the result from the picker activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            // getting the photos
            val photos: ArrayList<UnsplashPhoto>? =
                data?.getParcelableArrayListExtra(UnsplashPickerActivity.EXTRA_PHOTOS)
            // showing the preview
            mAdapter.setListOfPhotos(photos)
            // telling the user how many have been selected
            Toast.makeText(this, "number of selected photos: " + photos?.size, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        // dummy request code to identify the request
        private const val REQUEST_CODE = 123
    }
}
