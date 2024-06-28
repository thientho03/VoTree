package com.example.votree.products.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Transaction(
    var id: String = "",
    val customerId: String = "",
    val storeId: String = "",
    val productsMap: MutableMap<String, Int> = mutableMapOf(),
    var remainPrice: Double = 0.0,
    val status: String = "",
    val name: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    var totalAmount: Double = 0.0,
    val createdAt: Date = Date(),
) : Parcelable