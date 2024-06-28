package com.example.votree.products.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.databinding.PointTransactionAdapterBinding
import com.example.votree.products.models.PointTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PointTransactionAdapter(
    private val transactions: List<PointTransaction>
) : RecyclerView.Adapter<PointTransactionAdapter.PointTransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointTransactionViewHolder {
        val binding = PointTransactionAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PointTransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PointTransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    class PointTransactionViewHolder(
        private val binding: PointTransactionAdapterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: PointTransaction) {
            binding.pointTransactionTitleTv.text = transaction.description
            binding.pointTransactionDate.text = formatDate(transaction.transactionDate)

            val pointText = if (transaction.type == "earn") {
                "+${transaction.points}"
            } else {
                "-${transaction.points}"
            }
            binding.pointsTv.text = pointText

            // Set the color based on the type of transaction
            val context = binding.root.context
            val color = if (transaction.type == "earn") {
                context.getColor(R.color.md_theme_primary)
            } else {
                context.getColor(R.color.md_theme_outline)
            }
            binding.pointsTv.setTextColor(color)

            // Set the icon based on the type of transaction
            val icon = if (transaction.type == "earn") {
                R.drawable.ic_earn
            } else {
                R.drawable.ic_redeem_points
            }
        }

        private fun formatDate(date: Date): String {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return format.format(date)
        }
    }
}
