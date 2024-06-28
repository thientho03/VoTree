package com.example.votree.products.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R

abstract class ProductImageAdapter<T>(protected val items: List<T>) :
    RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder>() {

    abstract fun bindImage(imageView: ImageView, item: T)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_image_adapter, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        bindImage(holder.imageView, items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.productImageView)
    }
}

class ProductImageAdapterUri(imageUris: List<Uri>) : ProductImageAdapter<Uri>(imageUris) {
    override fun bindImage(imageView: ImageView, item: Uri) {
        imageView.setImageURI(item)
    }
}

class ProductImageAdapterString(imageUrls: List<String>) : ProductImageAdapter<String>(imageUrls) {
    override fun bindImage(imageView: ImageView, item: String) {
        Glide.with(imageView.context)
            .load(item)
            .centerCrop()
            .placeholder(R.drawable.img_placeholder)
            .into(imageView)
    }
}