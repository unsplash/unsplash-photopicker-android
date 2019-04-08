package com.unsplash.pickerandroid.photopicker.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import com.unsplash.pickerandroid.photopicker.R
import kotlinx.android.synthetic.main.activity_image_show.*

class PhotoShowActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_show)
        // loading the image thanks to its url
        Picasso.get().load(intent.getStringExtra(EXTRA_URL))
            .into(image_show_view)
        // click listener
        image_show_layout.setOnClickListener { supportFinishAfterTransition() }
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
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
