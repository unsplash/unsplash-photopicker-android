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
import com.unsplash.pickerandroid.photopicker.data.Photo
import kotlinx.android.synthetic.main.item_photo.view.*

/**
 * The photos recycler view adapter.
 * This is using the Android paging library to display an infinite list of photos.
 * This deals with either a single or multiple selection list.
 */
class PhotoAdapter constructor(context: Context, private val isMultipleSelection: Boolean) :
    PagedListAdapter<Photo, PhotoAdapter.PhotoViewHolder>(COMPARATOR) {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    private val mSelectedIndexes = ArrayList<Int>()

    private val mSelectedImages = ArrayList<Image>()

    private var mOnImageSelectedListener: OnImageSelectedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(mLayoutInflater.inflate(R.layout.item_photo, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // item
        val photo = getItem(position)
        if (photo != null) {
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
                mOnImageSelectedListener?.onImageSelected(mSelectedIndexes.size)
                // change title text
            }
            holder.itemView.setOnLongClickListener {
                mOnImageSelectedListener?.onImageLongPress(holder.imageView, photo.urls.regular)
                false
            }
        }
    }

    /**
     * Getter for the selected images.
     */
    fun getImages(): ArrayList<Image> {
        mSelectedImages.clear()
        for (index in mSelectedIndexes) {
            if (currentList != null && currentList!![index] != null) {
                val photo = currentList!![index]!!
                mSelectedImages.add(
                    Image(
                        photo.user.name,
                        photo.urls.thumb,
                        photo.urls.small,
                        photo.urls.regular,
                        photo.urls.full,
                        photo.urls.raw,
                        photo.links.download
                    )
                )
            }
        }
        return mSelectedImages
    }

    fun clearSelection() {
        mSelectedImages.clear()
        mSelectedIndexes.clear()
    }

    fun setOnImageSelectedListener(onImageSelectedListener: OnImageSelectedListener) {
        mOnImageSelectedListener = onImageSelectedListener
    }

    companion object {
        // diff util comparator
        val COMPARATOR = object : DiffUtil.ItemCallback<Photo>() {
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                oldItem == newItem
        }
    }

    /**
     * Photo view holder.
     */
    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: AspectRatioImageView = view.item_photo_image_view
        val txtView: TextView = view.item_photo_text_view
        val checkedImageView: ImageView = view.item_photo_checked_image_view
        val overlay: View = view.item_photo_overlay
    }
}