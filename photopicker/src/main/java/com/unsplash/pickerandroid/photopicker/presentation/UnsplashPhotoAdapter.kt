package com.unsplash.pickerandroid.photopicker.presentation

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.databinding.ItemUnsplashPhotoBinding

/**
 * The photos recycler view adapter.
 * This is using the Android paging library to display an infinite list of photos.
 * This deals with either a single or multiple selection list.
 */
class UnsplashPhotoAdapter(private val isMultipleSelection: Boolean) :
    PagingDataAdapter<UnsplashPhoto, UnsplashPhotoAdapter.PhotoViewHolder>(COMPARATOR) {

    private val mSelectedIndexes = mutableListOf<Int>()
    private val mSelectedImages = mutableListOf<UnsplashPhoto>()
    private var mOnPhotoSelectedListener: OnPhotoSelectedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            ItemUnsplashPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: PhotoViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && "selection" in payloads) {
            holder.checkedImageView.isInvisible = position !in mSelectedIndexes
            holder.overlay.isInvisible = position !in mSelectedIndexes
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // item
        getItem(position)?.let { photo ->
            // image
            holder.imageView.aspectRatio = photo.height.toDouble() / photo.width.toDouble()
            holder.itemView.setBackgroundColor(Color.parseColor(photo.color))
            Glide.with(holder.imageView.context)
                .load(photo.urls.small)
                .into(holder.imageView)

            // photograph name
            holder.txtView.text = photo.user.name

            // selected controls visibility
            holder.checkedImageView.isInvisible = holder.bindingAdapterPosition !in mSelectedIndexes
            holder.overlay.isInvisible = holder.bindingAdapterPosition !in mSelectedIndexes

            // click listener
            holder.itemView.setOnClickListener {
                // selected index(es) management
                if (position in mSelectedIndexes) {
                    mSelectedIndexes.remove(position)
                } else {
                    mSelectedIndexes.add(position)
                }
                if (isMultipleSelection) {
                    notifyItemChanged(position, "selection")
                }
                mOnPhotoSelectedListener?.onPhotoSelected(mSelectedIndexes.size)

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
    fun getImages(): List<UnsplashPhoto> {
        mSelectedImages.clear()
        for (index in mSelectedIndexes) {
            snapshot()[index]?.let {
                mSelectedImages.add(it)
            }
        }
        return mSelectedImages
    }

    fun clearSelection() {
        val previousSize = mSelectedIndexes.size
        mSelectedImages.clear()
        mSelectedIndexes.clear()

        notifyItemRangeChanged(0, previousSize, "selection")
    }

    fun setOnImageSelectedListener(onPhotoSelectedListener: OnPhotoSelectedListener) {
        mOnPhotoSelectedListener = onPhotoSelectedListener
    }

    companion object {
        // diff util comparator
        val COMPARATOR = object : DiffUtil.ItemCallback<UnsplashPhoto>() {
            override fun areContentsTheSame(
                oldItem: UnsplashPhoto,
                newItem: UnsplashPhoto
            ): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: UnsplashPhoto, newItem: UnsplashPhoto): Boolean =
                oldItem.id == newItem.id
        }
    }

    /**
     * UnsplashPhoto view holder.
     */
    class PhotoViewHolder(binding: ItemUnsplashPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val imageView: AspectRatioImageView = binding.itemUnsplashPhotoImageView
        val txtView: TextView = binding.itemUnsplashPhotoTextView
        val checkedImageView: ImageView = binding.itemUnsplashPhotoCheckedImageView
        val overlay: View = binding.itemUnsplashPhotoOverlay
    }
}