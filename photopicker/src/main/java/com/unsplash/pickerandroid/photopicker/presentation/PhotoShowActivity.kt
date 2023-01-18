package com.unsplash.pickerandroid.photopicker.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.unsplash.pickerandroid.photopicker.databinding.ActivityImageShowBinding

class PhotoShowActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityImageShowBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // loading the image thanks to its url
        Glide.with(this)
            .load(intent.getStringExtra(EXTRA_URL))
            .into(binding.imageShowView)

        // click listener
        binding.imageShowLayout.setOnClickListener { supportFinishAfterTransition() }

        onBackPressedDispatcher.addCallback {
            supportFinishAfterTransition()
        }
    }

    companion object {
        private const val EXTRA_URL = "EXTRA_URL"

        /**
         * @param callingContext the calling context
         * @param url the url of the image to show
         *
         * @return the intent needed to come to this activity
         */
        fun getStartingIntent(callingContext: Context, url: String): Intent {
            val intent = Intent(callingContext, PhotoShowActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            return intent
        }
    }
}
