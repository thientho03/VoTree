package com.example.votree.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.util.Date


@Parcelize
data class Report (
    var id: String,
    var userId: String,
    var reporterId: String,
    var tipId: String?,
    var productId: String?,
    var shortDescription: String,
    var content: String,
    var processStatus: Boolean,
    var processingState: String,
    var processingContent: String,
    var imageList: List<String>,
    var createdAt: Date = Date(),
    var updatedAt: Date = Date()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readBoolean(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        Date(parcel.readLong()),
        Date(parcel.readLong())
    )

    constructor() : this("", "", "", "", "", "", "", false, "", "", emptyList(), Date(), Date())

    companion object : Parceler<Report> {

        override fun Report.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(userId)
            parcel.writeString(reporterId)
            parcel.writeString(tipId)
            parcel.writeString(productId)
            parcel.writeString(shortDescription)
            parcel.writeString(content)
            parcel.writeBoolean(processStatus)
            parcel.writeString(processingState)
            parcel.writeString(processingContent)
            parcel.writeStringList(imageList)
            parcel.writeLong(createdAt.time)
            parcel.writeLong(updatedAt.time)
        }

        override fun create(parcel: Parcel): Report {
            return Report(parcel)
        }
    }
}