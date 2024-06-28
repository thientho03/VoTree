package com.example.votree.products.repositories

import com.example.votree.products.models.ProductReview
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Fetch all reviews for a specific product
    suspend fun getReviewsByProductId(productId: String): List<ProductReview> {
        return try {
            firestore.collection("products/$productId/reviews")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(ProductReview::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Add a new review to the database
    suspend fun addReview(review: ProductReview, productId: String): Boolean {
        return try {
            firestore.collection("products/$productId/reviews")
                .add(review)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Update an existing review
    suspend fun updateReview(reviewId: String, updatedReview: ProductReview, productId: String): Boolean {
        return try {
            firestore.collection("products/$productId/reviews")
                .document(reviewId)
                .set(updatedReview)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Delete a review
    suspend fun deleteReview(reviewId: String, productId: String): Boolean {
        return try {
            firestore.collection("products/$productId/reviews")
                .document(reviewId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}