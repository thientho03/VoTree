package com.example.votree.admin.interfaces

import android.view.View
import com.example.votree.models.Tip

interface OnItemClickListener {
    fun onItemClicked(view: View?, position: Int)
    fun onProductItemClicked(view: View?, position: Int)
    fun onTransactionItemClicked(view: View?, position: Int)
    fun onTipItemClicked(view: View?, position: Int)
    fun onAccountItemClicked(view: View?, position: Int)
    fun onReportItemClicked(view: View?, position: Int, processStatus: Boolean)
    fun searchItem(query: String) {}
}