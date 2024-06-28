package com.example.votree.notifications.view_holders

import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.NotificationAdapterBinding
import com.example.votree.notifications.adapters.NotificationAdapter
import com.example.votree.notifications.models.Notification
import com.example.votree.notifications.view_models.NotificationViewModel
import com.example.votree.products.models.Product
import com.example.votree.products.models.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationViewHolder(
    private val binding: NotificationAdapterBinding,
    private val listener: NotificationAdapter.OnNotificationClickListener,
    private val notificationViewModel: NotificationViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(notification: Notification) {
        binding.notificationTitleTv.text = notification.title
        binding.notificationContentTv.text = notification.content

        // Create a flag for types of notifications
        // Notification.title = "New Order", "Order Denied", "Order Delivered"
        val notificationType = notification.title

        // Format the notification time
        val formattedTime = formatNotificationTime(notification.createdAt)
        binding.notificationTimeTv.text = formattedTime

        updateNotificationBackground(notification)

        if (notification.orderId.isNotEmpty()) {
            notificationViewModel.fetchTransactionInfo(notification.orderId)
            notificationViewModel.fetchProductInfo(notification)
        }

        binding.root.setOnClickListener {
            listener.onNotificationClick(notification)
            markNotificationAsRead(notification)
            updateNotificationBackground(notification)
        }

        notificationViewModel.transactionData.observe(viewLifecycleOwner) { transaction ->
            updateTransactionInfo(transaction)
        }

        notificationViewModel.productData.observe(viewLifecycleOwner) { product ->
            updateProductInfo(product, notificationType)
        }
    }

    private fun updateTransactionInfo(transaction: Transaction?) {
         binding.notificationContentTv.text = transaction?.name
    }

    private fun updateProductInfo(product: Product?, notificationType: String) {
        if(notificationType == "New Order")
            binding.notificationTitleTv.text = "New Order: ${product?.productName}"
        else if (notificationType == "Order Denied") {
            binding.notificationTitleTv.text = "Order Denied: ${product?.productName}"
            binding.notificationTitleTv.setTextColor(binding.root.resources.getColor(R.color.md_theme_error, null))
            binding.notificationContentTv.text = "Your order has been denied by the store owner"
        } else if (notificationType == "Order Delivered") {
            binding.notificationTitleTv.text = "Order Delivered: ${product?.productName}"
        }
        else {
            binding.notificationTitleTv.text = product?.productName
        }

         Glide.with(binding.root)
             .load(product?.imageUrl?.get(0))
             .error(R.drawable.img_placeholder)
             .into(binding.notificationImageIv)
    }

    private fun updateNotificationBackground(notification: Notification) {
        if (notification.read) {
            binding.notificationLayout.setBackgroundColor(Color.parseColor("#F1F5EC"))
        } else {
            binding.notificationLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }
    }

    private fun markNotificationAsRead(notification: Notification) {
        notification.read = true
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            notificationViewModel.updateNotificationReadStatus(notification.id, true)
        }
    }

    private fun formatNotificationTime(createdAt: Date): String {
        val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
        return formatter.format(createdAt)
    }
}


