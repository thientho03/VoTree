package com.example.votree.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Tip (
    var id: String,
    var userId: String,
    var title: String,
    var shortDescription: String,
    var content: String,
    var imageList: List<String>,
    var vote_count: Int,
    var approvalStatus: Int,
    var createdAt: Date = Date(),
    var updatedAt: Date = Date()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readInt(),
        parcel.readInt(),
        Date(parcel.readLong()),
        Date(parcel.readLong())
    )

    constructor() : this("", "", "", "", "", emptyList(), 0, 0, Date(), Date())

    companion object : Parceler<Tip> {

        override fun Tip.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(userId)
            parcel.writeString(title)
            parcel.writeString(shortDescription)
            parcel.writeString(content)
            parcel.writeStringList(imageList)
            parcel.writeInt(vote_count)
            parcel.writeInt(approvalStatus)
            parcel.writeLong(createdAt.time)
            parcel.writeLong(updatedAt.time)
        }

        override fun create(parcel: Parcel): Tip {
            return Tip(parcel)
        }

    }
}