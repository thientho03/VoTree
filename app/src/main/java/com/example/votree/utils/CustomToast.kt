package com.example.votree.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.votree.R

object CustomToast {
    fun show(context: Context, message: String, type: ToastType) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast_layout, null)

        val text = layout.findViewById<TextView>(R.id.toast_text)
        val icon = layout.findViewById<ImageView>(R.id.toast_icon)
        val cardView = layout.findViewById<CardView>(R.id.toast_card)

        text.text = message

        when (type) {
            ToastType.SUCCESS -> {
                icon.setImageResource(R.drawable.done_24px)
                cardView.setCardBackgroundColor(context.resources.getColor(R.color.md_theme_primary_opacity_12))
            }
            ToastType.FAILURE -> {
                icon.setImageResource(R.drawable.ic_close_24dp)
                cardView.setCardBackgroundColor(context.resources.getColor(R.color.md_theme_errorContainer_mediumContrast))
            }
            ToastType.INFO -> {
                icon.setImageResource(R.drawable.ic_info)
                cardView.setCardBackgroundColor(context.resources.getColor(R.color.md_theme_primary_opacity_12))
            }
        }

        with (Toast(context)) {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }
}