package com.example.votree.notifications.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.votree.databinding.NotificationAdapterBinding
import com.example.votree.notifications.models.Notification
import com.example.votree.notifications.view_holders.NotificationViewHolder
import com.example.votree.notifications.view_models.NotificationViewModel


class NotificationAdapter(private val listener: OnNotificationClickListener) :
    ListAdapter<Notification, NotificationViewHolder>(NotificationDiffCallback()) {

    interface OnNotificationClickListener {
        fun onNotificationClick(notification: Notification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val notificationViewModel = NotificationViewModel()
        val viewLifecycleOwner = listener as androidx.lifecycle.LifecycleOwner
        return NotificationViewHolder(binding, listener, notificationViewModel, viewLifecycleOwner)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = getItem(position)
        holder.bind(notification)
    }
}

class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
    override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem == newItem
    }
}

