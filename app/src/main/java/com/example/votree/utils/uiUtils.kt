package com.example.votree.utils

import android.content.Context

class uiUtils {
    companion object{
        fun calculateNoOfColumns(context: Context): Int {
            val displayMetrics = context.resources.displayMetrics
            val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
            val columnWidthDp = 180 // Assume each item in the grid takes up 180dp
            return (screenWidthDp / columnWidthDp).toInt()
        }

    }
}