package com.example.votree.admin.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.models.Store
import com.example.votree.models.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class StoreDetailFragment : Fragment() {

    private val db = Firebase.firestore
    private var store: Store? = null
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_store_detail, container, false)
        val viewTransactionListButton = view.findViewById<TextView>(R.id.viewTransactionButton)
//        val viewDiscountsButton = view.findViewById<Button>(R.id.viewDiscountsButton)
        val viewTipListButton = view.findViewById<TextView>(R.id.listTipButton)
        val viewProductListButton = view.findViewById<TextView>(R.id.viewProductListButton)
        val topAppBar = (activity as AdminMainActivity).findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        topAppBar.title = "Store Information"
        topAppBar.setTitleTextColor(resources.getColor(R.color.md_theme_primary))

        viewTransactionListButton.setOnClickListener {
            val storeId = store?.id
            storeId?.let { id ->
                val dialogFragment = TransactionDialogFragment.newInstance(id)
                dialogFragment.show(parentFragmentManager, "TransactionDialogFragment")
            }
        }

        viewTipListButton.setOnClickListener {
            userId?.let { id ->
                val dialogFragment = TipDialogFragment.newInstance(id)
                dialogFragment.show(parentFragmentManager, "TipDialogFragment")
            }
        }

        viewProductListButton.setOnClickListener {
            val storeId = store?.id
            storeId?.let { id ->
                val dialogFragment = ProductDialogFragment.newInstance(id)
                dialogFragment.show(parentFragmentManager, "ProductDialogFragment")
            }
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        store = arguments?.getParcelable("store", Store::class.java)
        userId = arguments?.getString("userId")

        updateUI()
    }

    private fun updateUI() {
        store?.let { nonNullStore ->
            val storeAvatar = view?.findViewById<ImageView>(R.id.store_avatar)
            storeAvatar?.let {
                Glide.with(this)
                    .load(nonNullStore.storeAvatar)
                    .into(it)
            }

            storeAvatar?.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("imageUrl", nonNullStore.storeAvatar)

                val fragment = ImageFragment()
                fragment.arguments = bundle

                (activity as AdminMainActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            view?.findViewById<TextView>(R.id.store_name)?.text = nonNullStore.storeName
            view?.findViewById<TextView>(R.id.account_created_date)?.text = dateFormat(nonNullStore.createdAt.toString())
            view?.findViewById<TextView>(R.id.account_updated_date)?.text = dateFormat(nonNullStore.updatedAt.toString())
            view?.findViewById<TextView>(R.id.account_phone_number)?.text = nonNullStore.storePhoneNumber
            view?.findViewById<TextView>(R.id.account_email)?.text = nonNullStore.storeEmail
            view?.findViewById<TextView>(R.id.account_address)?.text = nonNullStore.storeLocation

            val copyPhone = view?.findViewById<TextView>(R.id.copy_phone)
            val copyEmail = view?.findViewById<TextView>(R.id.copy_email)
            val copyAddress = view?.findViewById<TextView>(R.id.copy_address)
            val phoneIcon = view?.findViewById<ImageView>(R.id.account_phone_icon)
            val emailIcon = view?.findViewById<ImageView>(R.id.account_email_icon)
            val account_position_icon = view?.findViewById<ImageView>(R.id.account_position_icon)

            phoneIcon?.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${nonNullStore.storePhoneNumber}")
                startActivity(intent)
            }
            emailIcon?.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:${nonNullStore.storeEmail}")
                startActivity(intent)
            }
            account_position_icon?.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("geo:0,0?q=${nonNullStore.storeLocation}")
                startActivity(intent)
            }

            copyPhone?.setOnClickListener {
                val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", nonNullStore.storePhoneNumber)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
            }
            copyEmail?.setOnClickListener {
                val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", nonNullStore.storeEmail)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
            }
            copyAddress?.setOnClickListener {
                val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", nonNullStore.storeLocation)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val inputDate = inputFormat.parse(date)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        return outputFormat.format(inputDate)
    }
}