package com.unsplash.pickerandroid.photopicker.presentation

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.unsplash.pickerandroid.photopicker.R

class PickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
    }

    companion object {
        /**
         * @return the intent needed to come to this activity
         */
        fun getStartingIntent(callingContext: Context): Intent {
            return Intent(callingContext, PickerActivity::class.java)
        }
    }
}
