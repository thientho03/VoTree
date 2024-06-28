package com.example.votree.admin.adapters

import android.annotation.SuppressLint
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Product

@Suppress("DEPRECATION")
class ProductListAdapter(private val listener: OnItemClickListener, private val isDialog: Boolean = false) :
    BaseListAdapter<Product>(listener) {

    override var singleitem_selection_position = -1

    override fun getLayoutId(): Int = R.layout.item_product

    override fun createViewHolder(itemView: View): BaseViewHolder {
        return ViewHolder(itemView)
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {

        override fun bind(item: Product) {
            super.bind(item)

            Glide.with(itemView.context)
                .load(item.imageUrl[0])
                .into(itemView.findViewById(R.id.product_list_item_avatar))

            itemView.findViewById<TextView>(R.id.product_name).text = item.productName
            itemView.findViewById<TextView>(R.id.product_rating).text = item.averageRate.toString()
            itemView.findViewById<TextView>(R.id.transaction_list_item_short_description).text = item.shortDescription
            itemView.findViewById<TextView>(R.id.product_price_after_sale_off).text = priceFormat((item.price * (100 - item.saleOff) / 100).toString())
            itemView.findViewById<TextView>(R.id.product_quantity).text = "x${item.currentQuantitySold}"
            itemView.findViewById<TextView>(R.id.total_quantity_sold).text = item.quantitySold.toString()

            // strikethrough price if sale off > 0
            if (item.saleOff > 0) {
                val spannableString = SpannableString(item.price.toString())
                spannableString.setSpan(StrikethroughSpan(), 0, item.price.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                itemView.findViewById<TextView>(R.id.product_price).text = spannableString
            } else {
                itemView.findViewById<TextView>(R.id.product_price).visibility = View.GONE
            }

            if (isDialog) {
                if (absoluteAdapterPosition == singleitem_selection_position) {
                    itemView.findViewById<TextView>(R.id.product_name).setTextColor(itemView.resources.getColor(R.color.md_theme_onPrimary))
                    itemView.findViewById<TextView>(R.id.product_rating).setTextColor(itemView.resources.getColor(R.color.md_theme_onPrimary))
//                    itemView.findViewById<TextView>(R.id.star_icon).imageTintList = itemView.resources.getColorStateList(R.color.md_theme_onPrimary)
                    itemView.findViewById<TextView>(R.id.payment_option).setTextColor(itemView.resources.getColor(R.color.md_theme_onPrimary))
                    itemView.findViewById<TextView>(R.id.total_quantity_sold).setTextColor(itemView.resources.getColor(R.color.md_theme_onPrimary))
                    itemView.setBackgroundResource(android.R.color.holo_green_light)
                } else {
                    itemView.findViewById<TextView>(R.id.product_name).setTextColor(itemView.resources.getColor(R.color.md_theme_primary))
                    itemView.findViewById<TextView>(R.id.product_rating).setTextColor(itemView.resources.getColor(R.color.md_theme_primary))
//                    itemView.findViewById<TextView>(R.id.star_icon).setTextColor(itemView.resources.getColor(R.color.md_theme_primary))
                    itemView.findViewById<TextView>(R.id.payment_option).setTextColor(itemView.resources.getColor(R.color.md_theme_primary))
                    itemView.findViewById<TextView>(R.id.total_quantity_sold).setTextColor(itemView.resources.getColor(R.color.md_theme_primary))
                    itemView.setBackgroundResource(android.R.color.transparent)
                }
            } else {
                itemView.setOnClickListener {
                    listener.onProductItemClicked(itemView, absoluteAdapterPosition)
                }
            }
        }
    }
}