package com.example.votree.products.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votree.products.models.ProductReview
import com.example.votree.products.repositories.ReviewRepository
import com.example.votree.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class ProductReviewListViewModel(private val reviewRepository: ReviewRepository, productId: String) : ViewModel() {
    private val _allReviews = MutableLiveData<List<ProductReview>>()
    val allReviews: LiveData<List<ProductReview>> = _allReviews

    private val _filteredReviews = MutableLiveData<List<ProductReview>>()
    val filteredReviews: LiveData<List<ProductReview>> = _filteredReviews

    // SingleLiveEvent for one-time UI events
    private val _uiEvent = SingleLiveEvent<String>()
    val uiEvent: LiveData<String> = _uiEvent

    init {
        fetchReviews(productId)
    }

    private fun fetchReviews(productId: String) {
        viewModelScope.launch {
            try {
                val reviews = reviewRepository.getReviewsByProductId(productId) // Replace with actual product ID
                _allReviews.value = reviews
                _filteredReviews.value = reviews // Initially, all reviews are shown
            } catch (e: Exception) {
                _uiEvent.value = "Failed to fetch reviews"
            }
        }
    }

    fun applyFilter(ratings: Set<Float>) {
        _filteredReviews.value = _allReviews.value?.filter { it.rating in ratings }
    }
}