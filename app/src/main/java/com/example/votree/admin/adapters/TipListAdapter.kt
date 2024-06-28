package com.example.votree.admin.adapters

import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Tip

class TipListAdapter(private val listener: OnItemClickListener, private val isDialog: Boolean = false) :
    BaseListAdapter<Tip>(listener) {

    override var singleitem_selection_position = -1

    override fun getLayoutId(): Int = R.layout.item_tip

    override fun createViewHolder(itemView: View): BaseViewHolder {
        return ViewHolder(itemView)
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {

        override fun bind(item: Tip) {
            super.bind(item)

            if (item.imageList.isNotEmpty() && Patterns.WEB_URL.matcher(item.imageList[0]).matches()) {
                Glide.with(itemView.context)
                    .load(item.imageList[0])
                    .into(itemView.findViewById(R.id.tip_list_item_avatar))
            } else {
                Glide.with(itemView.context)
                    .load(R.drawable.report_default)
                    .into(itemView.findViewById(R.id.tip_list_item_avatar))
            }

            when (item.approvalStatus) {
                -1 -> {
                    itemView.findViewById<LinearLayout>(R.id.tip_row_layout).background =
                        ContextCompat.getDrawable(itemView.context, R.color.md_theme_tertiaryContainer)
                    itemView.findViewById<ImageView>(R.id.tip_item_icon)
                        .setImageResource(R.drawable.red_tick)
                }
                1 -> {
                    itemView.findViewById<LinearLayout>(R.id.tip_row_layout).background =
                        ContextCompat.getDrawable(itemView.context, R.color.md_theme_primaryContainer)
                    itemView.findViewById<ImageView>(R.id.tip_item_icon)
                        .setImageResource(R.drawable.green_tick)
                }
                else -> {
                    itemView.findViewById<LinearLayout>(R.id.tip_row_layout).background =
                        ContextCompat.getDrawable(itemView.context, R.color.md_theme_onPrimary)
                    itemView.findViewById<ImageView>(R.id.tip_item_icon)
                        .setImageResource(R.drawable.baseline_arrow_right_24)
                }
            }
            itemView.findViewById<TextView>(R.id.tip_list_item_title).text = item.title
            itemView.findViewById<TextView>(R.id.tip_list_item_short_description).text = item.shortDescription

            if (isDialog) {
                if (absoluteAdapterPosition == singleitem_selection_position) {
                    itemView.setBackgroundResource(android.R.color.holo_green_light)
                } else {
                    itemView.setBackgroundResource(android.R.color.transparent)
                }
            } else {
                itemView.setOnClickListener {
                    listener.onTipItemClicked(itemView, absoluteAdapterPosition)
                }
            }
        }
    }
}
