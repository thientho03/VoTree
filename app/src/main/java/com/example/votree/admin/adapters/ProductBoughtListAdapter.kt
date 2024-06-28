package com.example.votree.admin.adapters

import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Product
import com.example.votree.models.User
import com.example.votree.products.repositories.TransactionRepository
import com.google.firebase.firestore.FirebaseFirestore

class ProductBoughtListAdapter(private val listener: OnItemClickListener, private val isDialog: Boolean = false) :
    BaseListAdapter<Product>(listener) {

    override var singleitem_selection_position = -1

    override fun getLayoutId(): Int = R.layout.item_product_bought

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
            itemView.findViewById<TextView>(R.id.total_money_value).text = priceFormat((item.price * item.quantitySold * (100 - item.saleOff) / 100).toString())

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
                    itemView.setBackgroundResource(android.R.color.holo_green_light)
                } else {
                    itemView.setBackgroundResource(android.R.color.transparent)
                }
            } else {
                itemView.setOnClickListener {
                    listener.onProductItemClicked(itemView, absoluteAdapterPosition)
                }
            }
        }
    }

//    fun formatPrice(price: String): String {
//        val priceString = price.reversed()
//        var result = ""
//        for (i in priceString.indices) {
//            result += priceString[i]
//            if ((i + 1) % 3 == 0 && i != priceString.length - 1) {
//                result += "."
//            }
//        }
//        return "đ$result".reversed()
//    }

//    fun fetchUserDataFromFirestore() {
//        db.collection("users")
//            .addSnapshotListener { snapshots, e ->
//                if (e != null) {
//                    Log.w("AccountListFragment", "listen:error", e)
//                    return@addSnapshotListener
//                }
//
//                itemList.clear()
//                for (doc in snapshots!!) {
//                    val account = doc.toObject(User::class.java)
//                    account.id = doc.id
//                    itemList.add(account)
//                }
//                adapter.setData(itemList)
//            }
//    }
}