package com.example.votree.admin.adapters

import android.util.Patterns
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.User

class AccountListAdapter(private var listener: OnItemClickListener) :
    BaseListAdapter<User>(listener) {

    override var singleitem_selection_position = -1 // no use

    override fun getLayoutId(): Int = R.layout.item_account

    override fun createViewHolder(itemView: View): BaseViewHolder {
        return ViewHolder(itemView)
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {

        override fun bind(item: User) {
            super.bind(item)
            if (Patterns.WEB_URL.matcher(item.avatar).matches()) {
                Glide.with(itemView.context)
                    .load(item.avatar)
                    .into(itemView.findViewById(R.id.account_list_item_avatar))
            } else {
                Glide.with(itemView.context)
                    .load(R.drawable.avatar_default_2)
                    .into(itemView.findViewById(R.id.account_list_item_avatar))
            }

            itemView.findViewById<TextView>(R.id.account_list_item_name).text = item.username
            itemView.findViewById<TextView>(R.id.account_list_item_role).text = item.role
            itemView.setOnClickListener {
                listener.onAccountItemClicked(itemView, absoluteAdapterPosition)
            }
        }
    }
}