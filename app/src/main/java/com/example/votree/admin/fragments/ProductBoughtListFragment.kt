package com.example.votree.admin.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.adapters.ProductBoughtListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Product
import com.example.votree.models.Transaction

class ProductBoughtListFragment : BaseListFragment<Product>(), OnItemClickListener {

    override val adapter: BaseListAdapter<Product> by lazy { ProductBoughtListAdapter(this) }
    override val itemList = mutableListOf<Product>()
    override val collectionName = "products"
    private var currentTransactionId = ""

    override fun getLayoutId(): Int = R.layout.fragment_list

    override fun fetchDataFromFirestore() {
        val bundle = this.arguments
        if (bundle != null) {
            currentTransactionId = bundle.getString("transactionId").toString()
        }
        super.fetchDataFromFirestore()
        itemList.clear()
        if (currentTransactionId != "") {
            db.collection("transactions").whereEqualTo("id", currentTransactionId)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("ProductBoughtListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    for (doc in snapshots!!) {
                        val transaction = doc.toObject(Transaction::class.java)
                        db.collection(collectionName).whereIn("id", transaction.productsMap.keys.toList())
                            .addSnapshotListener { snapshots2, e2 ->
                                if (e2 != null) {
                                    Log.w("ProductBoughtListFragment", "listen:error", e)
                                    return@addSnapshotListener
                                }

                                for (doc2 in snapshots2!!) {
                                    val product = doc2.toObject(Product::class.java)
                                    product.id = doc2.id
                                    itemList.add(product)
                                }
                                adapter.setData(itemList)
                            }
                    }
                }
        }
        else {
            db.collection(collectionName)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("ProductBoughtListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    for (doc in snapshots!!) {
                        val product = doc.toObject(Product::class.java)
                        product.id = doc.id
                        itemList.add(product)
                    }
                    adapter.setData(itemList)
                }
        }
    }

    override fun onProductItemClicked(view: View?, position: Int) {
        (activity as AdminMainActivity).onProductItemClicked(view, position)

        val fragment = ProductDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("product", adapter.getItem(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("product_list_fragment").commit()
    }
}
