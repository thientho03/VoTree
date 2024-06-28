package com.example.votree.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.votree.R
import com.github.chrisbanes.photoview.PhotoView

class ImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoView = view.findViewById<PhotoView>(R.id.fullScreenImageView)

        // Retrieve image URL passed from the activity
        val imageUrl = arguments?.getString("imageUrl")

        // Load the image using Glide or Picasso into PhotoView
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(photoView)
        }

        // Enable zooming capabilities
        photoView.maximumScale = 10f // Maximum scale factor for zooming
        photoView.mediumScale = 2f // Medium scale factor for zooming
        photoView.minimumScale = 0.5f // Minimum scale factor for zooming
        photoView.setOnMatrixChangeListener { matrix ->
            // Handle matrix changes if needed
        }
    }
}


