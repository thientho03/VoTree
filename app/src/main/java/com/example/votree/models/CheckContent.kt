package com.example.votree.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class CheckContent(
    var id: String = "",
    val tipId: String = "",
    val tipContent: String = "",
    val response: String = "",
) : Parcelable