package com.example.votree.users.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.models.Friend
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FriendAdapter(private val friends: List<Friend>, private val onClick: (Friend) -> Unit) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(itemView, onClick)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)
    }

    override fun getItemCount() = friends.size

    class FriendViewHolder(itemView: View, val onClick: (Friend) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewFriendName)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.textViewLastMessage)
        private val timeTextView: TextView = itemView.findViewById(R.id.textViewLastMessageTime)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.imageViewProfile)

        fun bind(friend: Friend) {
            nameTextView.text = friend.name
            lastMessageTextView.text = friend.lastMessage ?: "No message"

            friend.lastMessageTime?.let { timestamp ->
                val calendar = Calendar.getInstance()
                val currentTime = calendar.timeInMillis
                val messageTime = Date(timestamp)

                val dateFormat = if (isToday(messageTime, currentTime)) {
                    SimpleDateFormat("HH:mm", Locale.getDefault())
                } else {
                    SimpleDateFormat("dd/MM", Locale.getDefault())
                }

                val dateTime = dateFormat.format(messageTime)
                timeTextView.text = dateTime
            } ?: run {
                timeTextView.text = ""
            }

            if (friend.lastMessageTime == 0L) {
                timeTextView.text = ""
            }

            Glide.with(itemView.context).load(friend.avatar).into(avatarImageView)

            itemView.setOnClickListener { onClick(friend) }
        }

        private fun isToday(messageTime: Date, currentTime: Long): Boolean {
            val messageCalendar = Calendar.getInstance().apply { time = messageTime }
            val currentCalendar = Calendar.getInstance().apply { timeInMillis = currentTime }

            return messageCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    messageCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                    messageCalendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH)
        }
    }
}
