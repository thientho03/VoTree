package com.example.votree.users.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.OrderManagementAdapterBinding
import com.example.votree.products.models.Transaction
import com.example.votree.products.repositories.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class OrderManagementAdapter(
    private val orders: List<Transaction>,
    private val layoutInflater: LayoutInflater
) : RecyclerView.Adapter<OrderManagementAdapter.OrderViewHolder>() {
    private val firestore = FirebaseFirestore.getInstance()
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(transaction: Transaction)
    }

    inner class OrderViewHolder(private val binding: OrderManagementAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClickListener?.onItemClick(orders[adapterPosition])
            }
        }

        fun bind(order: Transaction) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.orderTimeTv.text = dateFormat.format(order.createdAt)
            binding.orderFromTv.text = order.name

            // Set the status text and text color based on order status
            when (order.status) {
                "pending" -> {
                    binding.orderStatusTv.text = "PENDING"
                    binding.orderStatusTv.setTextColor(Color.parseColor("#B6C324"))
                }

                "delivered" -> {
                    binding.orderStatusTv.text = "DELIVERED"
                    binding.orderStatusTv.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.md_theme_primary
                        )
                    )
                }

                "denied" -> {
                    binding.orderStatusTv.text = "DENIED"
                    binding.orderStatusTv.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.md_theme_errorContainer_mediumContrast
                        )
                    )
                }

                else -> {
                    binding.orderStatusTv.text = order.status
                    binding.orderStatusTv.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.md_theme_tertiary
                        )
                    )
                }
            }

            val productRepository = ProductRepository(firestore)
            val firstProductId = order.productsMap.keys.firstOrNull()
            if (firstProductId != null) {
                // Use a coroutine to call the suspend function
                CoroutineScope(Dispatchers.IO).launch {
                    val product = productRepository.getProduct(firstProductId)

                    withContext(Dispatchers.Main) {
                        binding.orderNameTv.text = product.productName
                        Glide.with(binding.root.context)
                            .load(product.imageUrl[0])
                            .into(binding.orderImageIv)
                    }
                }
            } else {
                // Handle the case when there are no products in the order
                binding.orderImageIv.setImageResource(R.drawable.img_placeholder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = OrderManagementAdapterBinding.inflate(layoutInflater, parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    override fun getItemCount(): Int = orders.size
}