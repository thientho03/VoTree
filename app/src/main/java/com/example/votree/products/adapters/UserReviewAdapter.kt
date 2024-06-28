package com.example.votree.products.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.products.models.ProductReview
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserReviewAdapter(
    var userReviews: List<ProductReview>,
    private val coroutineScope: CoroutineScope // Pass a CoroutineScope from the Activity or Fragment
) : RecyclerView.Adapter<UserReviewAdapter.UserReviewViewHolder>() {

    class UserReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImageIv: ImageView = view.findViewById(R.id.productImage_iv)
        val userNameTv: TextView = view.findViewById(R.id.userName_tv)
        val userReviewTv: TextView = view.findViewById(R.id.userReview_tv)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_user_review_adapter, parent, false)
        return UserReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserReviewViewHolder, position: Int) {
        val userReview = userReviews[position]
        holder.userReviewTv.text = userReview.reviewText
        holder.ratingBar.rating = userReview.rating

        // Load the product image if available
        userReview.imageUrl?.let { imageUrl ->
            Glide.with(holder.productImageIv.context)
                .load(imageUrl)
                .into(holder.productImageIv)
        }

        // Fetch the user's name using the userId
        coroutineScope.launch(Dispatchers.IO) {
            val userName = fetchUserName(userReview.userId)
            withContext(Dispatchers.Main) {
                holder.userNameTv.text = userName ?: "Unknown User"
            }
        }
    }

    override fun getItemCount(): Int = userReviews.size

    // Function to fetch the user's name from Firestore
    private suspend fun fetchUserName(userId: String): String? {
        val db = FirebaseFirestore.getInstance()
        val docSnapshot = db.collection("users").document(userId).get().await()
        return docSnapshot.getString("fullName") ?: "Unknown User"
    }

    fun setData(newReviews: List<ProductReview>) {
        userReviews = newReviews
        notifyDataSetChanged()
    }
}
