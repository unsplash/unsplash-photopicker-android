package com.unsplash.pickerandroid.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.unsplash.pickerandroid.example.databinding.ActivityMainBinding
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.presentation.UnsplashPickerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mAdapter: PhotoAdapter
    private val binding: ActivityMainBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mAdapter = PhotoAdapter().also {
            binding.mainRecyclerView.adapter = it
        }
        // recycler view configuration
        binding.mainRecyclerView.apply {
            setHasFixedSize(true)
            itemAnimator = null
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            adapter = mAdapter
        }

        // on the pick button click, we start the library picker activity
        // we are expecting a result from it so we start it for result
        binding.mainPickButton.setOnClickListener {
            pickerLauncher.launch(
                UnsplashPickerActivity.getStartingIntent(
                    this,
                    !binding.mainSingleRadioButton.isChecked
                )
            )
        }
    }

    private val pickerLauncher = registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            // getting the photos
            val photos = getResult(it.data)
            // showing the preview
            mAdapter.setListOfPhotos(photos)

            // telling the user how many have been selected
            Toast.makeText(this, "number of selected photos: " + photos?.size, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getResult(data: Intent?): List<UnsplashPhoto>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data?.getParcelableArrayListExtra(
                UnsplashPickerActivity.EXTRA_PHOTOS,
                UnsplashPhoto::class.java
            )
        } else {
            data?.getParcelableArrayListExtra(UnsplashPickerActivity.EXTRA_PHOTOS)
        }
    }
}
