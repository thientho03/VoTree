package com.example.votree.products.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductReview(
    var id: String = "",
    val transactionId: String = "",
    val userId: String = "",
    val reviewText: String = "",
    val rating: Float = 0f,
    val imageUrl: String? = null
) : Parcelable