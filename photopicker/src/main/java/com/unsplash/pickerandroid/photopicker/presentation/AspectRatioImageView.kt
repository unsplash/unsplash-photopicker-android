package com.unsplash.pickerandroid.photopicker.presentation

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class AspectRatioImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    var aspectRatio: Double = -1.0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (aspectRatio == -1.0) return
        val width = measuredWidth
        val height = (width * aspectRatio).toInt()
        if (height == measuredHeight) return
        setMeasuredDimension(width, height)
    }
}