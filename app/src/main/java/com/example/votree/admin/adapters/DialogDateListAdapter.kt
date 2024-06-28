package com.example.votree.admin.adapters

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import com.example.votree.R
import com.example.votree.admin.interfaces.OnItemClickListener

class DialogDateListAdapter(private val nDates: List<String>, listener: OnItemClickListener) :
    BaseListAdapter<String>(listener) {

    override var singleitem_selection_position = -1

    override fun getLayoutId(): Int = R.layout.item_date

    override fun createViewHolder(itemView: View): BaseViewHolder {
        return ViewHolder(itemView)
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        override fun bind(item: String) {
            super.bind(item)
            // Set the background color based on the selected position
            if (absoluteAdapterPosition == singleitem_selection_position) {
                itemView.setBackgroundResource(android.R.color.holo_red_light)
            } else {
                itemView.setBackgroundResource(android.R.color.transparent)
            }

            dateTextView.text = item
        }
    }
}
