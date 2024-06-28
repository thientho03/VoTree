package com.example.votree.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Transaction(
    var id: String = "",
    val customerId: String = "",
    val storeId: String = "",
    val productsMap: MutableMap<String, Int> = mutableMapOf(),
    val remainPrice: Double = 0.0,
    val status: String = "",
    val name: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val createdAt: Date = Date(),
    val totalAmount: Double = 0.0,
) : Parcelable