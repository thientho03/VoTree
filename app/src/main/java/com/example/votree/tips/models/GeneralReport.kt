package com.example.votree.tips.models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class GeneralReport (
    var id: String = "",
    var userId: String = "",
    var reporterId: String = "",
    var tipId: String = "",
    var productId: String = "",
    var shortDescription: String = "",
    var content: String = "",
    var processStatus: Boolean = false,
    var processingMethod: String = "",
    var imageList: MutableList<String> = mutableListOf(""),
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable {}