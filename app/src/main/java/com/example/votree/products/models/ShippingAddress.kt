package com.example.votree.products.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShippingAddress(
    var id: String = "",
    val recipientName: String = "",
    val recipientPhoneNumber: String = "",
    val recipientAddress: String = "",
    val default: Boolean = false
) : Parcelable
