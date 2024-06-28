package com.example.votree.admin.fragments

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.models.Product
import com.example.votree.models.Store
import com.example.votree.models.Transaction
import com.example.votree.models.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class TransactionDetailFragment : Fragment() {
    private var transaction: Transaction? = null
    private var currentTransactionId: String = ""
    private var productBoughtList: String = ""
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_detail, container, false)

//        customerDetailButton?.setOnClickListener {
//            val accountId = transaction?.customerId
//            val fragment = AccountDetailFragment()
//            val bundle = Bundle().apply {
//                putParcelable("account", adapter.getItem(position))
//            }
//            fragment.arguments = bundle
//            (activity as AdminMainActivity).supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, fragment)
//        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transaction = arguments?.getParcelable("transaction", Transaction::class.java)
        setTransactionId(transaction?.id ?: "")
        updateUI()
    }

    private fun updateUI() {
//        val viewStoreProfileButton = view?.findViewById<Button>(R.id.view_store_profile)
        val customerDetailButton = view?.findViewById<FrameLayout>(R.id.frame_layout_transaction_customer)
        val productListButton = view?.findViewById<FrameLayout>(R.id.frame_layout_transaction_products)
        val avatarClick: RelativeLayout? = view?.findViewById(R.id.avatarClick)

        if (currentTransactionId != "" && transaction != null) {
            productBoughtList = ""
            var isFirstProduct = true
            for ((productId, _) in transaction!!.productsMap) {
                db.collection("products").whereEqualTo("id", productId)
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            Log.w("TransactionDetailFragment", "listen:error", e)
                            return@addSnapshotListener
                        }

                        for (doc in snapshots!!) {
                            val product = doc.toObject(Product::class.java)
                            if (product.id == productId) {
                                productBoughtList += if (isFirstProduct) {
                                    isFirstProduct = false
                                    product.productName
                                } else {
                                    ", ${product.productName}"
                                }
                            }
                        }
                        val transactionProductBought: TextView? = view?.findViewById(R.id.transactionProducts)

                        transactionProductBought?.text = "Product(s): $productBoughtList"

                    }
            }
        } else {
            productBoughtList = "No product bought"
        }

        // List products
        productListButton?.setOnClickListener {
            val fragment = ProductBoughtListFragment()
            val bundle = Bundle().apply {
                putString("transactionId", currentTransactionId)
            }
            fragment.arguments = bundle
            (activity as AdminMainActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("transaction_detail_fragment")
                .commit()
        }

        // Get store information
        db.collection("stores").document(transaction?.storeId!!)
            .get()
            .addOnSuccessListener { storeDocument ->
                val store = storeDocument.toObject(Store::class.java)
                if (store != null) {
                    view?.findViewById<ImageView>(R.id.account_avatar).let {
                        Glide.with(this)
                            .load(store.storeAvatar)
                            .into(it!!)
                    }
                    view?.findViewById<TextView>(R.id.store_name)?.text = store.storeName

                    // Fetch the store owner data
                    db.collection("users").whereEqualTo("storeId", store.id)
                        .get()
                        .addOnSuccessListener { userDocuments ->
                            for (userDocument in userDocuments) {
                                val owner = userDocument.toObject(User::class.java)
                                if (owner.storeId == store.id) {
                                    avatarClick?.setOnClickListener {
                                        val fragment = AccountDetailFragment()
                                        val bundle = Bundle().apply {
                                            putParcelable("account", owner)
                                        }
                                        fragment.arguments = bundle
                                        (activity as AdminMainActivity).supportFragmentManager.beginTransaction()
                                            .replace(R.id.fragment_container, fragment)
                                            .addToBackStack("transaction_detail_fragment")
                                            .commit()
                                    }
                                    break // Exit loop after finding the owner
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("TransactionDetailFragment", "Error fetching store owner", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TransactionDetailFragment", "Error fetching store details", e)
            }

        // Fetch the customer data
        db.collection("users").document(transaction?.customerId!!)
            .get()
            .addOnSuccessListener { userDocument ->
                val customer = userDocument.toObject(User::class.java)
                if (customer != null) {
                    view?.findViewById<TextView>(R.id.transactionCustomer)?.text = "Customer: ${customer.fullName}"
                    customerDetailButton?.setOnClickListener {
                        val fragment = AccountDetailFragment()
                        val bundle = Bundle().apply {
                            putParcelable("account", customer)
                        }
                        fragment.arguments = bundle
                        (activity as AdminMainActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack("transaction_detail_fragment")
                            .commit()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TransactionDetailFragment", "Error fetching customer details", e)
            }

        transaction?.let { nonNullTransaction ->
            val transactionAddress: TextView? = view?.findViewById(R.id.address)
            val transactionOrderedDate: TextView? = view?.findViewById(R.id.ordered_date)
            val transactionPaymentOption: TextView? = view?.findViewById(R.id.payment_option_value)
//            val transactionVoucherDiscount: TextView? = view?.findViewById(R.id.voucher_discount_value)
            val transactionTotalPaymentValue: TextView? = view?.findViewById(R.id.total_payment_value)

            transactionAddress?.text = "Address: ${nonNullTransaction.address}"
            transactionOrderedDate?.text = "Ordered on ${dateFormat(nonNullTransaction.createdAt.toString())}"
            if (nonNullTransaction.remainPrice == 0.0) {
                transactionPaymentOption?.text = "Prepay"
            } else {
                transactionPaymentOption?.text = if (nonNullTransaction.remainPrice.compareTo(nonNullTransaction.totalAmount) < 0) "Prepay and Cash" else "Cash"
            }

            transactionTotalPaymentValue?.text = priceFormat(nonNullTransaction.totalAmount.toString())

        }
    }

    private fun dateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val inputDate = inputFormat.parse(date)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        return outputFormat.format(inputDate)
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

    fun setTransactionId(transactionId: String) {
        this.currentTransactionId = transactionId
    }
}
