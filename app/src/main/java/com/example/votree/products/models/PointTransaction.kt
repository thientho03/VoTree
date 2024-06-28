package com.example.votree.products.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class PointTransaction(
    var id: String = "",
    val userId: String = "",
    val points: Int = 0,
    val type: String = "", // "earn" or "redeem"
    val description: String = "",
    val transactionDate: Date = Date()
) : Parcelable {
    override fun toString(): String {
        return "PointTransaction(id=$id, userId='$userId', points=$points, type='$type', description='$description', transactionDate=$transactionDate)"
    }
}
