package com.example.votree.users.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Store(
    var id: String = "",
    val storeName: String = "",
    var storeAvatar: String = "",
    val storeLocation: String = "",
    val storeEmail: String = "",
    val storePhoneNumber: String = "",
    val discountCodeIdList: List<String> = mutableListOf(),
    val transactionIdList: List<String> = mutableListOf(),
    var active: Boolean = true,
    val createdAt: Date = Date(),
    var updatedAt: Date = Date()
) : Parcelable {
    override fun toString(): String {
        return "Store(id=$id, storeName='$storeName', storeLocation='$storeLocation')"
    }
}
