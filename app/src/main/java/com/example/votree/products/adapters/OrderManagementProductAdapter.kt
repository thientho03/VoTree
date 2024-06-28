package com.example.votree.products.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.products.models.Product
import com.example.votree.products.models.Transaction
import com.example.votree.products.view_models.ProductViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderManagementProductAdapter(
    private val context: Context,
    private val transaction: Transaction,
    private val productViewModel: ProductViewModel
) : RecyclerView.Adapter<OrderManagementProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.checkout_product_adapter, parent, false)
        Log.d("OrderManagementProductAdapter", "onCreateViewHolder")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productId = transaction.productsMap.keys.toList()[position]
        val quantity = transaction.productsMap.values.toList()[position]

        CoroutineScope(Dispatchers.IO).launch {
            val product = productViewModel.getProduct(productId)
            withContext(Dispatchers.Main) {
                holder.bind(product, quantity)
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productNameTextView: TextView = itemView.findViewById(R.id.productName_tv)
        private val productPriceTextView: TextView = itemView.findViewById(R.id.productPrice_tv)
        private val productQuantityTextView: TextView =
            itemView.findViewById(R.id.productQuantity_tv)
        private val productImageView: ImageView = itemView.findViewById(R.id.productImage_iv)

        fun bind(product: Product?, quantity: Int) {
            // Bind product data to views
            productNameTextView.text = product?.productName ?: ""
            productPriceTextView.text =
                product?.let { context.getString(R.string.price_format, it.price) } ?: ""
            productQuantityTextView.text = context.getString(R.string.quantity_format, quantity)
            Glide.with(itemView)
                .load(product?.imageUrl?.get(0))
                .into(productImageView)
        }
    }

    override fun getItemCount(): Int {
        return transaction.productsMap.size
    }
}
