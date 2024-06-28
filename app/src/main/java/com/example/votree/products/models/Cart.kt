package com.example.votree.products.models

import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cart(
    var id: String = "",
    val userId: String = "",
    val productsMap: MutableMap<String, Int> = mutableMapOf(),
    var totalPrice: Double = 0.0
) : Parcelable {
    fun addProduct(productId: String, quantity: Int) {
        productsMap[productId] = quantity
    }

    fun removeCart() {
        Log.d("Cart", "Cart removed")
        productsMap.clear()
    }
}
