package com.example.votree.tips.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.tips.models.Comment

class TipCommentAdapter : ListAdapter<Comment, TipCommentViewHolder>(TipCommentComparators()) {
    class TipCommentComparators() : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem === newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipCommentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val template : View = layoutInflater.inflate(R.layout.tip_comment_item, parent, false)
        return TipCommentViewHolder(template)
    }

    override fun onBindViewHolder(holder: TipCommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.commentAuthor.text = comment.fullName
        holder.commentContent.text = comment.content

        if (comment.avatar.isNotEmpty())
            Glide.with(holder.itemView)
                .load(comment.avatar)
                .placeholder(R.drawable.img_placeholder)
                .into(holder.commentAvatar)
    }
}

class TipCommentViewHolder(itemview: View) : ViewHolder(itemview) {
    val commentAvatar = itemview.findViewById<ImageView>(R.id.tip_comment_avatar)
    val commentAuthor = itemview.findViewById<TextView>(R.id.tip_comment_name)
    val commentContent = itemview.findViewById<TextView>(R.id.tip_comment_content)
}