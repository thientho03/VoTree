package com.example.votree.admin.fragments

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.models.Product
import com.example.votree.models.Store
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class ProductDetailFragment : Fragment() {

    private val db = Firebase.firestore
    private var product: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_product_detail, container, false)
//        val hideButton = view?.findViewById<Button>(R.id.hideButton)
//
//        hideButton?.setOnClickListener {
//            db.collection("products").document(product!!.id).update("isActive", false)
//                .addOnSuccessListener {
//                    activity?.supportFragmentManager?.popBackStack("product_list_fragment", POP_BACK_STACK_INCLUSIVE)
//
//                }
//                .addOnFailureListener { e ->
//                    e.printStackTrace()
//                }
//        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        product = arguments?.getParcelable("product", Product::class.java)

        updateUI()
    }

    private fun updateUI() {

        db.collection("stores")
            .addSnapshotListener { userSnapshots, userError ->
                if (userError != null) {
                    return@addSnapshotListener
                }

                for (userDoc in userSnapshots!!) {
                    val store = userDoc.toObject(Store::class.java)
                    if (store.id == product?.storeId) {
                        view?.findViewById<TextView>(R.id.storeName)?.text = store.storeName
                    }
                }
            }

        product?.let { nonNullProduct ->
            val productImage = view?.findViewById<ImageView>(R.id.productImage)
            val hideButton: Button? = view?.findViewById(R.id.hideButton)
            view?.findViewById<TextView>(R.id.productName)?.text = nonNullProduct.productName
            view?.findViewById<RatingBar>(R.id.productRating_rb)?.rating = nonNullProduct.averageRate.toFloat()
            view?.findViewById<TextView>(R.id.productRating)?.text = nonNullProduct.averageRate.toString()
            view?.findViewById<TextView>(R.id.productSoldQuantity)?.text = nonNullProduct.quantitySold.toString()
            view?.findViewById<TextView>(R.id.description)?.text = nonNullProduct.description
            view?.findViewById<TextView>(R.id.transaction_list_item_title)?.text = "Price: ${nonNullProduct.price}"
//            hideButton?.text = if (nonNullProduct.isActive) "  Hide this product" else "  Unhide this product"
//            hideButton?.backgroundTintList = if (nonNullProduct.isActive) resources.getColorStateList(R.color.yellow) else resources.getColorStateList(R.color.md_theme_onSurfaceVariant)
            hideButton?.backgroundTintList = when (nonNullProduct.isActive) {
                true -> resources.getColorStateList(R.color.yellow)
                false -> resources.getColorStateList(R.color.md_theme_onSurfaceVariant)
            }
            hideButton?.text = when (nonNullProduct.isActive) {
                true -> "  Hide this product"
                false -> "  Unhide this product"
            }
            hideButton?.setOnClickListener {
                db.collection("products").document(product!!.id).update("isActive", !nonNullProduct.isActive)
                    .addOnSuccessListener {
//                        hideButton.text = if (nonNullProduct.isActive) "  Unhide this product" else "  Hide this product"
//                        hideButton.backgroundTintList = if (nonNullProduct.isActive) resources.getColorStateList(R.color.md_theme_onSurfaceVariant) else resources.getColorStateList(R.color.yellow)
                        activity?.supportFragmentManager?.popBackStack("product_list_fragment", POP_BACK_STACK_INCLUSIVE)
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
            view?.findViewById<ImageView>(R.id.productImage)?.let {
                Glide.with(this)
                    .load(nonNullProduct.imageUrl[0])
                    .into(it)
            }
            productImage?.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("imageUrl", nonNullProduct.imageUrl[0])

                val fragment = ImageFragment()
                fragment.arguments = bundle

                (activity as AdminMainActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
