package com.unsplash.pickerandroid.photopicker.presentation

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

// this file holds all the method extensions of the project

/**
 * Opens the keyboard using the view itself.
 */
fun View.openKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * Closes the keyboard using the view itself.
 */
fun View.closeKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}
