package com.example.votree.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.votree.R

object ProgressDialogUtils {

    private var progressDialog: AlertDialog? = null

    fun showLoadingDialog(context: Context) {
        Log.d("ProgressDialogUtils", "showLoadingDialog")
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        builder.setView(inflater.inflate(R.layout.progress_dialog, null))
        builder.setCancelable(false)

        progressDialog = builder.create()
        progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog?.show()
    }

    fun hideLoadingDialog() {
        Log.d("ProgressDialogUtils", "hideLoadingDialog")
        progressDialog?.dismiss()
    }
}