package com.unsplash.pickerandroid.example

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.unsplash.pickerandroid.example.databinding.ItemPhotoBinding
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto

class PhotoAdapter : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    private var mListOfPhotos: List<UnsplashPhoto> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // item
        val photo = mListOfPhotos[position]
        // image background
        holder.itemView.setBackgroundColor(Color.parseColor(photo.color))
        // loading the photo
        Picasso.get().load(photo.urls.small)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return mListOfPhotos.size
    }

    fun setListOfPhotos(listOfPhotos: List<UnsplashPhoto>?) {
        if (listOfPhotos != null) {
            mListOfPhotos = listOfPhotos
            notifyDataSetChanged()
        }
    }

    /**
     * UnsplashPhoto view holder.
     */
    class PhotoViewHolder(binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.itemPhotoIv
    }
}