package com.example.votree.products.models

sealed class ProductItem {
    data class ProductHeader(
        var storeId: String = "",
        val shopName: String,
        var isChecked: Boolean = false
    ) : ProductItem()

    data class ProductData(
        val product: Product,
        val quantity: Int,
        var isChecked: Boolean = false
    ) : ProductItem()
}
