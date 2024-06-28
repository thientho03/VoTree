package com.example.votree.products.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.votree.products.repositories.ReviewRepository
import com.example.votree.products.view_models.ProductReviewListViewModel

class ProductReviewListViewModelFactory(private val reviewRepository: ReviewRepository, productId: String) : ViewModelProvider.Factory {
    private val productId = productId

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductReviewListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductReviewListViewModel(reviewRepository, productId = productId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}