package com.unsplash.pickerandroid.photopicker.presentation

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

fun EditText.textChangeFlow(): Flow<String> {
    return callbackFlow {
        val watcher = doAfterTextChanged { editable ->
            trySend(editable?.toString().orEmpty())
        }

        trySend(text.toString())

        awaitClose {
            println("Closed text changed listener")
            removeTextChangedListener(watcher)
        }
    }
}