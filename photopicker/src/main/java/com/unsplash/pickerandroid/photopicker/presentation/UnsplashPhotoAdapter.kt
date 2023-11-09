package com.unsplash.pickerandroid.photopicker.presentation

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.unsplash.pickerandroid.photopicker.R
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto

/**
 * The photos recycler view adapter.
 * This is using the Android paging library to display an infinite list of photos.
 * This deals with either a single or multiple selection list.
 */
class UnsplashPhotoAdapter constructor(context: Context, private val isMultipleSelection: Boolean) :
    PagedListAdapter<UnsplashPhoto, UnsplashPhotoAdapter.PhotoViewHolder>(COMPARATOR) {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    private val mSelectedIndexes = ArrayList<Int>()

    private val mSelectedImages = ArrayList<UnsplashPhoto>()

    private var mOnPhotoSelectedListener: OnPhotoSelectedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(mLayoutInflater.inflate(R.layout.item_unsplash_photo, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // item
        getItem(position)?.let { photo ->
            // image
            holder.imageView.aspectRatio = photo.height.toDouble() / photo.width.toDouble()
            holder.itemView.setBackgroundColor(Color.parseColor(photo.color))
            Picasso.get().load(photo.urls.small)
                .into(holder.imageView)
            // photograph name
            holder.txtView.text = photo.user.name
            // selected controls visibility
            holder.checkedImageView.visibility =
                    if (mSelectedIndexes.contains(holder.adapterPosition)) View.VISIBLE else View.INVISIBLE
            holder.overlay.visibility =
                    if (mSelectedIndexes.contains(holder.adapterPosition)) View.VISIBLE else View.INVISIBLE
            // click listener
            holder.itemView.setOnClickListener {
                // selected index(es) management
                if (mSelectedIndexes.contains(holder.adapterPosition)) {
                    mSelectedIndexes.remove(holder.adapterPosition)
                } else {
                    if (!isMultipleSelection) mSelectedIndexes.clear()
                    mSelectedIndexes.add(holder.adapterPosition)
                }
                if (isMultipleSelection) {
                    notifyDataSetChanged()
                }
                mOnPhotoSelectedListener?.onPhotoSelected(mSelectedIndexes.size)
                // change title text
            }
            holder.itemView.setOnLongClickListener {
                photo.urls.regular?.let {
                    mOnPhotoSelectedListener?.onPhotoLongPress(holder.imageView, it)
                }
                false
            }
        }
    }

    /**
     * Getter for the selected images.
     */
    fun getImages(): ArrayList<UnsplashPhoto> {
        mSelectedImages.clear()
        for (index in mSelectedIndexes) {
            currentList?.get(index)?.let {
                mSelectedImages.add(it)
            }
        }
        return mSelectedImages
    }

    fun clearSelection() {
        mSelectedImages.clear()
        mSelectedIndexes.clear()
    }

    fun setOnImageSelectedListener(onPhotoSelectedListener: OnPhotoSelectedListener) {
        mOnPhotoSelectedListener = onPhotoSelectedListener
    }

    companion object {
        // diff util comparator
        val COMPARATOR = object : DiffUtil.ItemCallback<UnsplashPhoto>() {
            override fun areContentsTheSame(oldItem: UnsplashPhoto, newItem: UnsplashPhoto): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: UnsplashPhoto, newItem: UnsplashPhoto): Boolean =
                oldItem == newItem
        }
    }

    /**
     * UnsplashPhoto view holder.
     */
    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: AspectRatioImageView = view.findViewById(R.id.item_unsplash_photo_image_view)
        val txtView: TextView = view.findViewById(R.id.item_unsplash_photo_text_view)
        val checkedImageView: ImageView = view.findViewById(R.id.item_unsplash_photo_checked_image_view)
        val overlay: View = view.findViewById(R.id.item_unsplash_photo_overlay)
    }
}