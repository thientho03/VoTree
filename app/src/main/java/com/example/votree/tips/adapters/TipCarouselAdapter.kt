package com.example.votree.tips.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.tips.onProductTipClickListener
import com.example.votree.tips.models.ProductTip
import com.google.android.material.textview.MaterialTextView

class TipCarouselAdapter(
    private val listener: onProductTipClickListener
) : ListAdapter<ProductTip, TipCarouselAdapter.TipCarouselViewHolder>(TipCarouselItemComparators()) {
    inner class TipCarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.carousel_image_view)
        val titleView: MaterialTextView = itemView.findViewById(R.id.carousel_item_text)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TipCarouselAdapter.TipCarouselViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val template : View = layoutInflater.inflate(R.layout.tip_carousel_item, parent, false)
        return TipCarouselViewHolder(template)
    }

    override fun onBindViewHolder(holder: TipCarouselAdapter.TipCarouselViewHolder, position: Int) {
        val tip = getItem(position)
        Glide.with(holder.itemView)
            .load(tip.imageList[0])
            .placeholder(R.drawable.img_placeholder)    // Chỗ này không có placeholder là không load được, không hiểu tại sao :))
            .into(holder.imageView)
        holder.titleView.text = tip.title

        holder.itemView.setOnClickListener {
            listener.onProductTipClick(tip)
        }
    }

    class TipCarouselItemComparators : DiffUtil.ItemCallback<ProductTip>() {
        override fun areItemsTheSame(oldItem: ProductTip, newItem: ProductTip): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: ProductTip, newItem: ProductTip): Boolean {
            return oldItem === newItem
        }
    }
}