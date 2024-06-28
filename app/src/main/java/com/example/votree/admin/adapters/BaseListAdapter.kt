package com.example.votree.admin.adapters

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.admin.interfaces.OnItemClickListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

abstract class BaseListAdapter<T>(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<BaseListAdapter<T>.BaseViewHolder>() {

    private var items = emptyList<T>()
    private var respectiveList = emptyList<Int>()
    protected abstract var singleitem_selection_position: Int
    val db = Firebase.firestore

    abstract inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClicked(itemView, position)
                }
            }
        }

        open fun bind(item: T) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
        return createViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    abstract fun getLayoutId(): Int

    abstract fun createViewHolder(itemView: View): BaseViewHolder

    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: List<T>) {
        this.items = items
        Log.d("BaseListAdapter", "setData: $items")
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: List<T>, respectiveList: List<Int>) {
        this.items = items
        this.respectiveList = respectiveList
        notifyDataSetChanged()
    }

    fun getItem(position: Int): T {
        return items[position]
    }

    fun getSelectedPosition(): Int {
        return singleitem_selection_position
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedPosition(position: Int) {
        singleitem_selection_position = position
        notifyDataSetChanged()
    }

    fun priceFormat(price: String): String {
        val formattedPrice = StringBuilder()
        val reversedPrice = price.reversed()

        for (i in reversedPrice.indices) {
            formattedPrice.append(reversedPrice[i])
            if ((i + 1) % 3 == 0 && (i + 1) != reversedPrice.length) {
                formattedPrice.append(',')
            }
        }

        return "$${formattedPrice.reverse()}"
    }

    fun dateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val inputDate = inputFormat.parse(date)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        return outputFormat.format(inputDate)
    }
}

