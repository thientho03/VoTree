package com.example.votree.products.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.products.models.Product
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.users.repositories.StoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(), Filterable {

    interface OnProductClickListener {
        fun onProductClick(product: Product)
    }

    private var productList = emptyList<Product>()
    private var filteredProductList = productList
    private var listener: OnProductClickListener? = null
    private var products: MutableList<Product> = mutableListOf()

    fun setOnProductClickListener(listener: OnProductClickListener) {
        this.listener = listener
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var productName = itemView.findViewById<TextView>(R.id.productName_tv)
        var productPrice = itemView.findViewById<TextView>(R.id.price_tv)
        var productRating = itemView.findViewById<RatingBar>(R.id.productRating_rb)
        var quantityOfSold = itemView.findViewById<TextView>(R.id.productSold_tv)
        var productListLayout = itemView.findViewById<View>(R.id.productListLayout)
        val productImage = itemView.findViewById<ImageView>(R.id.productImage_iv)
        val storeName = itemView.findViewById<TextView>(R.id.storeName_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.product_card, parent, false)
        )
    }

    override fun getItemCount(): Int = filteredProductList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
                filteredProductList = if (charString.isEmpty()) {
                    productList
                } else {
                    val filteredList = ArrayList<Product>()
                    for (product in productList) {
                        if (product.productName.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(product)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredProductList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredProductList = results?.values as List<Product>
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentItem = filteredProductList[position]
        val storeId = currentItem.storeId

        val storeRepository = StoreRepository()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val store = storeRepository.fetchStore(storeId)
                withContext(Dispatchers.Main) {
                    holder.storeName.text = store.storeName
                }
            } catch (e: Exception) {
                Log.e("ProductAdapter", "Error fetching store details", e)
            }
        }

        val productRepository = ProductRepository(FirebaseFirestore.getInstance())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val productRating = productRepository.getAverageProductRating(currentItem.id)
                withContext(Dispatchers.Main) {
                    holder.productRating.rating = productRating
                }
            } catch (e: Exception) {
                Log.e("ProductAdapter", "Error fetching store details", e)
            }
        }


        holder.itemView.apply {
            holder.productName.text = currentItem.productName
            holder.productPrice.text = currentItem.price.toString()
            holder.quantityOfSold.text = currentItem.quantitySold.toString()
            Glide.with(this)
                .load(currentItem.imageUrl?.get(0))
                .placeholder(R.drawable.img_placeholder)
                .into(holder.productImage)
        }

        holder.productListLayout.setOnClickListener {
            listener?.onProductClick(currentItem)
        }
    }

    fun setData(product: List<Product>) {
        this.productList = product
        this.filteredProductList = product
        notifyDataSetChanged()
    }

    fun addData(newProducts: List<Product>) {
        val currentSize = products.size
        products.addAll(newProducts)
        notifyItemRangeInserted(currentSize, newProducts.size)
    }
}