package com.example.votree.admin.fragments

import android.graphics.PorterDuff
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.toLowerCase
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.models.Product
import com.example.votree.models.Report
import com.example.votree.models.Store
import com.example.votree.models.Tip
import com.example.votree.models.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale


class ReportDetailFragment : Fragment() {

    private val db = Firebase.firestore
    private var report: Report? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report_detail, container, false)
        val unresolveButton = view?.findViewById<Button>(R.id.unresolveButton)
        val warnButton = view?.findViewById<Button>(R.id.warnButton)
        val resolvedButton = view?.findViewById<Button>(R.id.resolvedButton)

        val avatarClick: RelativeLayout? = view?.findViewById(R.id.avatarClick)
        avatarClick?.setOnClickListener {
            val fragment = AccountDetailFragment()
            db.collection("users").document(report!!.reporterId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(User::class.java)
                        fragment.arguments = Bundle().apply {
                            putParcelable("account", user)
                        }
                        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
                        (activity as AdminMainActivity).setCurrentFragment(AccountDetailFragment())
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("report_detail_fragment").commit()
                    }
                }
        }

        unresolveButton?.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_feedback, null)
            val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setTitle("Take notes something?")
            val alertDialog = alertDialogBuilder.show()
            val editTextFeedback = dialogView.findViewById<EditText>(R.id.editTextFeedback)
            val buttonReject = dialogView.findViewById<Button>(R.id.buttonReject)
            val buttonClose = dialogView.findViewById<Button>(R.id.buttonClose)
            buttonReject.text = "Unresolve"

            buttonReject.setOnClickListener {
                db.collection("reports").document(report!!.id).update("processingState", "Unresolved", "processingContent", editTextFeedback.text.toString())
                    .addOnSuccessListener {
                        activity?.supportFragmentManager?.popBackStack("report_list_fragment", POP_BACK_STACK_INCLUSIVE)
                    }
                alertDialog.dismiss()
            }

            buttonClose.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        warnButton?.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_feedback, null)
            val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setTitle("Take notes something?")
            val alertDialog = alertDialogBuilder.show()
            val editTextFeedback = dialogView.findViewById<EditText>(R.id.editTextFeedback)
            val buttonReject = dialogView.findViewById<Button>(R.id.buttonReject)
            val buttonClose = dialogView.findViewById<Button>(R.id.buttonClose)
            buttonReject.text = "Warn"

            buttonReject.setOnClickListener {
                db.collection("reports").document(report!!.id).update("processingContent", editTextFeedback.text.toString())
                    .addOnSuccessListener {
                        activity?.supportFragmentManager?.popBackStack("report_list_fragment", POP_BACK_STACK_INCLUSIVE)
                    }
                alertDialog.dismiss()
            }

            buttonClose.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        resolvedButton?.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_feedback, null)
            val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setTitle("Take notes something?")
            val alertDialog = alertDialogBuilder.show()
            val editTextFeedback = dialogView.findViewById<EditText>(R.id.editTextFeedback)
            val buttonReject = dialogView.findViewById<Button>(R.id.buttonReject)
            val buttonClose = dialogView.findViewById<Button>(R.id.buttonClose)
            buttonReject.text = "Resolved"


            buttonReject.setOnClickListener {
                db.collection("reports").document(report!!.id).update("processingState", "Resolved", "processingContent", editTextFeedback.text.toString())
                    .addOnSuccessListener {
                        activity?.supportFragmentManager?.popBackStack("report_list_fragment", POP_BACK_STACK_INCLUSIVE)
                    }
                alertDialog.dismiss()
            }

            buttonClose.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        report = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("report", Report::class.java)
        } else {
            arguments?.getParcelable("report", Report::class.java)
        }

        updateUI()
    }

    private fun updateUI() {
        report?.let { nonNullReport ->
            val processingState = view?.findViewById<TextView>(R.id.processingState)
            val processingContentIcon = view?.findViewById<ImageView>(R.id.processing_content_icon)
            val processingContent = view?.findViewById<TextView>(R.id.contentProcessingMethod)
            val frameCover = view?.findViewById<View>(R.id.frame_cover)

            if (nonNullReport.processingContent == "" || nonNullReport.processingContent == "null") {
                processingContent?.visibility = View.GONE
                processingContentIcon?.visibility = View.GONE
                frameCover?.visibility = View.GONE
            } else {
                processingContent?.text = nonNullReport.processingContent
            }

            if (nonNullReport.processingState.lowercase() == "resolved") {
                processingState?.text = "Resolved"
                processingState?.setTextColor(resources.getColor(R.color.md_theme_primary))
                processingState?.setBackgroundResource(R.color.md_theme_primaryFixedDim)
                context?.let { ContextCompat.getColor(it, R.color.md_theme_primary) }
                    ?.let { processingContentIcon?.setColorFilter(it, PorterDuff.Mode.SRC_IN) }
                frameCover?.setBackgroundColor(resources.getColor(R.color.md_theme_primaryFixedDim))
            } else if (nonNullReport.processingState.lowercase() == "unresolved") {
                processingState?.text = "Unresolved"
                processingState?.setTextColor(resources.getColor(R.color.md_theme_error))
                processingState?.setBackgroundResource(R.color.md_theme_errorContainer)
                context?.let { ContextCompat.getColor(it, R.color.md_theme_error) }
                    ?.let { processingContentIcon?.setColorFilter(it, PorterDuff.Mode.SRC_IN) }
                frameCover?.setBackgroundColor(resources.getColor(R.color.md_theme_errorContainer))
            } else {
                processingState?.text = "Pending"
                processingState?.setTextColor(resources.getColor(R.color.md_theme_onPrimary))
                processingState?.setBackgroundResource(R.color.md_theme_pending)
                context?.let { ContextCompat.getColor(it, R.color.yellow) }
                    ?.let { processingContentIcon?.setColorFilter(it, PorterDuff.Mode.SRC_IN) }
                frameCover?.setBackgroundColor(resources.getColor(R.color.md_theme_pending))
            }

            val viewDetailButton = view?.findViewById<Button>(R.id.viewDetailButton)

            if (nonNullReport.tipId != null && nonNullReport.tipId != "") {
                val fragment = TipDetailFragment()
                db.collection("ProductTip").document(nonNullReport.tipId!!).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val tip = document.toObject(Tip::class.java)
                            viewDetailButton?.setOnClickListener {
                                fragment.arguments = Bundle().apply {
                                    putParcelable("tip", tip)
                                }
                                val fragmentManager = (activity as FragmentActivity).supportFragmentManager
                                (activity as AdminMainActivity).setCurrentFragment(TipDetailFragment())
                                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("report_detail_fragment").commit()
                            }
                        }
                    }
            } else if (nonNullReport.productId != null && nonNullReport.productId != "") {
                val fragment = ProductDetailFragment()
                db.collection("products").document(nonNullReport.productId!!).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            viewDetailButton?.setOnClickListener {
                                val bundle = Bundle().apply {
                                    putParcelable("product", document.toObject(Product::class.java))
                                }
                                fragment.arguments = bundle

                                val fragmentManager = (activity as FragmentActivity).supportFragmentManager

                                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("report_detail_fragment").commit()
                            }
                        }
                    }
            } else {
                val fragment = AccountDetailFragment()
                db.collection("stores").document(nonNullReport.userId).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val store = document.toObject(Store::class.java)
                            db.collection("users").whereEqualTo("storeId", store?.id).get()
                                .addOnSuccessListener { documents ->
                                    if (documents != null) {
                                        for (doc in documents) {
                                            val user = doc.toObject(User::class.java)
                                            user.id = doc.id
                                            viewDetailButton?.setOnClickListener {
                                                val bundle = Bundle().apply {
                                                    putParcelable("account", user)
                                                }
                                                fragment.arguments = bundle

                                                val fragmentManager = (activity as FragmentActivity).supportFragmentManager

                                                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("report_detail_fragment").commit()
                                            }
                                        }
                                    }
                                }
                        }
                    }
            }

            val reportImage = view?.findViewById<ImageView>(R.id.reportImage)

            if (nonNullReport.imageList.isNotEmpty() && Patterns.WEB_URL.matcher(nonNullReport.imageList[0]).matches()) {
                reportImage?.let { imageView ->
                    Glide.with(this)
                        .load(nonNullReport.imageList[0])
                        .into(imageView)
                }
                reportImage?.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("imageUrl", nonNullReport.imageList[0])

                    val fragment = ImageFragment()
                    fragment.arguments = bundle

                    (activity as AdminMainActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            } else {
                reportImage?.let { imageView ->
                    Glide.with(this)
                        .load(R.drawable.report_default)
                        .into(imageView)
                }
                reportImage?.visibility = View.GONE
            }

            if (nonNullReport.tipId != null && nonNullReport.tipId != "") {
                view?.findViewById<TextView>(R.id.reportType)?.text = "Tip"
            } else if (nonNullReport.productId != null && nonNullReport.productId != "") {
                view?.findViewById<TextView>(R.id.reportType)?.text = "Product"
            } else {
                view?.findViewById<TextView>(R.id.reportType)?.text = "User"
            }

            db.collection("users").document(nonNullReport.reporterId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(User::class.java)
                        view?.findViewById<TextView>(R.id.userName)?.text = user?.username
                        view?.findViewById<TextView>(R.id.store_role)?.text = user?.role
                        view?.findViewById<ImageView>(R.id.reporter_avatar)?.let {
                            Glide.with(this)
                                .load(user?.avatar)
                                .into(it)
                        }
                    }
                }

            view?.findViewById<TextView>(R.id.report_content)?.text = nonNullReport.content

            val reportedDate = dateFormat(nonNullReport.createdAt.toString())
            view?.findViewById<TextView>(R.id.textViewDate)?.text = reportedDate
        }
    }

    private fun dateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val inputDate = inputFormat.parse(date)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        return outputFormat.format(inputDate)
    }

    fun getReporterId(): String? {
        return report?.reporterId
    }
}