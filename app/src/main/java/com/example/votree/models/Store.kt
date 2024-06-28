package com.example.votree.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Parcelize
data class Store (
    var id: String,
    var storeName: String,
    var storeLocation: String,
    var storeEmail: String,
    var storePhoneNumber: String,
    var storeAvatar: String = "",
    var discountCodeIdList: List<String>,
    var transactionIdList: List<String>,
    var createdAt: Date = Date(),
    var updatedAt: Date = Date(),
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.createStringArrayList()!!,
        Date(parcel.readLong()),
        Date(parcel.readLong()),
    )

    constructor() : this("", "", "", "", "", "", emptyList(), emptyList(), Date(), Date())

    companion object : Parceler<Store> {

        override fun Store.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(storeName)
            parcel.writeString(storeLocation)
            parcel.writeString(storeEmail)
            parcel.writeString(storePhoneNumber)
            parcel.writeString(storeAvatar)
            parcel.writeStringList(discountCodeIdList)
            parcel.writeStringList(transactionIdList)
            parcel.writeLong(createdAt.time)
            parcel.writeLong(updatedAt.time)
        }

        override fun create(parcel: Parcel): Store {
            return Store(parcel)
        }
    }
}