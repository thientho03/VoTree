package com.example.votree.users.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.products.activities.ProductReviewActivity
import com.example.votree.products.models.Transaction
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.products.repositories.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderItemAdapter(
    var transactions: List<Transaction>,
    private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val transactionRepository = TransactionRepository(firestore)
    private val productRepository = ProductRepository(firestore)
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    class OrderItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val shopNameTv: TextView = view.findViewById(R.id.shopName_tv)
        val statusTv: TextView = view.findViewById(R.id.status_tv)
        val quantityTv: TextView = view.findViewById(R.id.quantity_tv)
        val totalTv: TextView = view.findViewById(R.id.total_tv)
        val reviewBtn: Button = view.findViewById(R.id.review_btn)
        val productQuantityTv: TextView = view.findViewById(R.id.productQuantity_tv)
        val productNameTv: TextView = view.findViewById(R.id.productName_tv)
        val productPriceTv: TextView = view.findViewById(R.id.productPrice_tv)
        val productImageIv: ImageView = view.findViewById(R.id.productImage_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_order_item_adapter, parent, false) // Correct the layout resource if necessary
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val transaction = transactions[position]

        // Set the status and total price
        setStatusTextColor(holder.statusTv, transaction.status)
        holder.statusTv.text = transaction.status

        // Use a coroutine to fetch the shop name and calculate the total quantity
        coroutineScope.launch(Dispatchers.IO) {
            val shopName = transactionRepository.fetchShopName(transaction.storeId)
            val totalQuantity = transactionRepository.calculateTotalQuantity(transaction.productsMap)
            val totalPrice = transactionRepository.calculateTotalPrice(transaction.productsMap)
            val productId = transaction.productsMap.keys.firstOrNull()
            val product = productRepository.getProduct(productId)

            withContext(Dispatchers.Main) {
                // Update the UI on the main thread
                holder.shopNameTv.text = shopName ?: "Unknown Shop"
                holder.quantityTv.text = totalQuantity.toString()
                holder.totalTv.text = totalPrice.toString()

                holder.productQuantityTv.text = transaction.productsMap.values.firstOrNull().toString()
                holder.productNameTv.text = product.productName
                holder.productPriceTv.text = product.price.toString()
                // Load the product image
                 Glide.with(holder.itemView)
                     .load(product.imageUrl[0])
                     .placeholder(R.drawable.img_placeholder)
                     .into(holder.productImageIv)

                // Set the review button visibility and click listener
                if (transaction.status == "delivered") {
                    holder.reviewBtn.visibility = View.VISIBLE
                    holder.reviewBtn.setOnClickListener {
                        coroutineScope.launch(Dispatchers.IO) {
                            val isReviewed = transactionRepository.isReviewSubmitted(transaction.id, userId)
                            withContext(Dispatchers.Main) {
                                val intent = Intent(holder.itemView.context, ProductReviewActivity::class.java)
                                intent.putExtra("isSubmitted", isReviewed)
                                intent.putExtra("transactionId", transaction.id)
                                holder.itemView.context.startActivity(intent)
                            }
                        }
                    }
                } else {
                    holder.reviewBtn.visibility = View.GONE
                }
            }
        }

        coroutineScope.launch(Dispatchers.IO){
            val isReviewed = transactionRepository.isReviewSubmitted(transaction.id, userId)
            withContext(Dispatchers.Main){
                if (isReviewed){
                    holder.reviewBtn.text = "View Review"
                } else {
                    holder.reviewBtn.text = "Write Review"
                }
                holder.reviewBtn.setOnClickListener {
                    val intent = Intent(holder.itemView.context, ProductReviewActivity::class.java)
                    intent.putExtra("isSubmitted", isReviewed)
                    intent.putExtra("transactionId", transaction.id)
                    holder.itemView.context.startActivity(intent)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTransactions(newTransactions: List<Transaction>) {
        this.transactions = newTransactions
        notifyDataSetChanged()
    }

    private fun setStatusTextColor(statusTv: TextView, status: String) {
        when (status) {
            "pending" -> statusTv.setTextColor(statusTv.context.getColor(R.color.md_theme_secondaryContainer))
            "denied" -> statusTv.setTextColor(statusTv.context.getColor(R.color.md_theme_errorContainer_mediumContrast))
            "delivered" -> statusTv.setTextColor(statusTv.context.getColor(R.color.md_theme_primary_opacity_12))
            else -> statusTv.setTextColor(statusTv.context.getColor(R.color.md_theme_onSurface_mediumContrast))
        }
    }

    override fun getItemCount(): Int = transactions.size
}
