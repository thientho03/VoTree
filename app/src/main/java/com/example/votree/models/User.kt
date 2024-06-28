package com.example.votree.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class User (
    var id: String,
    var username: String,
    var fullName: String,
    var password: String,
    var phoneNumber: String,
    var address: String,
    var email: String,
    var avatar: String,
    var active: Boolean,
    var role: String,
    var storeId: String,
    var expirePremium: Date = Date(),
    var accumulatePoint: Int,
    var totalRevenue: Int,
    var transactionIdList: List<String>,
    var createdAt: Date = Date(),
    var updatedAt: Date = Date(),
    var expireBanDate: Date = Date(),
    var announcedMessage: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readBoolean(),
        parcel.readString()!!,
        parcel.readString()!!,
        Date(parcel.readLong()),
        parcel.readInt(),
        parcel.readInt(),
        parcel.createStringArrayList()!!,
        Date(parcel.readLong()),
        Date(parcel.readLong()),
        Date(parcel.readLong()),
        parcel.readString()!!,
    )

    constructor() : this("", "", "", "", "", "", "", "", false, "", "", Date(), 0, 0, emptyList(), Date(), Date(), Date(), "")

    companion object : Parceler<User> {

        override fun User.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(username)
            parcel.writeString(fullName)
            parcel.writeString(password)
            parcel.writeString(phoneNumber)
            parcel.writeString(address)
            parcel.writeString(email)
            parcel.writeString(avatar)
            parcel.writeBoolean(active)
            parcel.writeString(role)
            parcel.writeString(storeId)
            parcel.writeLong(expirePremium.time)
            parcel.writeInt(accumulatePoint)
            parcel.writeInt(totalRevenue)
            parcel.writeStringList(transactionIdList)
            parcel.writeLong(createdAt.time)
            parcel.writeLong(updatedAt.time)
            parcel.writeLong(expireBanDate.time)
            parcel.writeString(announcedMessage)
        }

        override fun create(parcel: Parcel): User {
            return User(parcel)
        }

    }
}