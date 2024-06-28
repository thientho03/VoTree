package com.example.votree.admin.fragments

import android.content.ContentValues
import android.icu.text.SimpleDateFormat
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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.models.CheckContent
import com.example.votree.models.Store
import com.example.votree.models.Tip
import com.example.votree.models.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class TipDetailFragment : Fragment() {

    private val db = Firebase.firestore
    private var tip: Tip? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tip_detail_admin, container, false)
        val approveButton = view?.findViewById<Button>(R.id.approveButton)
        val rejectButton = view?.findViewById<Button>(R.id.rejectButton)

        approveButton?.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Aprrove this tip?")
                .setMessage("Are you sure you want to approve this tip?")
                .setPositiveButton("Yes") { _, _ ->
                    db.collection("ProductTip").document(tip!!.id).update("approvalStatus", 1)
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
        }

        rejectButton?.setOnClickListener {
            // Inflate the custom dialog layout
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_feedback, null)

            // Create a dialog
            val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setTitle("Reject this tip?")

            // Set click listener for the reject button in the dialog
            val alertDialog = alertDialogBuilder.show()
            val editTextFeedback = dialogView.findViewById<EditText>(R.id.editTextFeedback)
            val buttonReject = dialogView.findViewById<Button>(R.id.buttonReject)
            val buttonClose = dialogView.findViewById<Button>(R.id.buttonClose)

            buttonReject.setOnClickListener {
                val feedback = editTextFeedback.text.toString()
                // Perform rejection action with feedback
                db.collection("ProductTip").document(tip!!.id).update("approvalStatus", -1, "feedback", feedback)
                    .addOnSuccessListener {
                        // Dismiss the dialog
                        alertDialog.dismiss()
                        // Pop back to tip list fragment
//                        activity?.supportFragmentManager?.popBackStack("tip_list_fragment", POP_BACK_STACK_INCLUSIVE)
                    }
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

        tip = arguments?.getParcelable("tip", Tip::class.java)

        updateUI()
    }

    private fun updateUI() {
        tip?.let { nonNullTip ->
            val tipStatusTextView: TextView? = view?.findViewById(R.id.tipStatus)
            val rejectButton: Button? = view?.findViewById(R.id.rejectButton)
            val approveButton: Button? = view?.findViewById(R.id.approveButton)
            val avatarClick: RelativeLayout? = view?.findViewById(R.id.avatarClick)
            val tipImage = view?.findViewById<ImageView>(R.id.tipImage)
            val tipShortDes: TextView? = view?.findViewById(R.id.tipShortDescription)
            val responseFromAI: TextView? = view?.findViewById(R.id.responseFromAI)
            val frameOfAIResponse: LinearLayout? = view?.findViewById(R.id.frameAI)

            if (nonNullTip.approvalStatus < 1) {
                frameOfAIResponse?.visibility = View.VISIBLE
                view?.findViewById<View>(R.id.upFrame)?.visibility = View.VISIBLE
                view?.findViewById<View>(R.id.downFrame)?.visibility = View.VISIBLE
                db.collection("checkContent")
                    .whereEqualTo("tipId", nonNullTip.id)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            responseFromAI?.text = "No response from AI"
                        } else {
                            val checkContent = querySnapshot.documents[0].toObject(CheckContent::class.java)
                            responseFromAI?.text = transformParagraph(checkContent?.response!!)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(ContentValues.TAG, "Error getting documents: ", e)
                    }
            } else {
                frameOfAIResponse?.visibility = View.GONE
                view?.findViewById<View>(R.id.upFrame)?.visibility = View.GONE
                view?.findViewById<View>(R.id.downFrame)?.visibility = View.GONE
            }

            tipImage?.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("imageUrl", nonNullTip.imageList[0])

                val fragment = ImageFragment()
                fragment.arguments = bundle

                (activity as AdminMainActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            tipShortDes?.text = nonNullTip.shortDescription

            db.collection("users").document(nonNullTip.userId)
                .get()
                .addOnSuccessListener { userDoc ->
                    val user = userDoc.toObject(User::class.java)
                    view?.findViewById<TextView>(R.id.userName)?.text = user?.fullName
                    view?.findViewById<ImageView>(R.id.tip_list_item_avatar)?.let {
                        Glide.with(this)
                            .load(user?.avatar)
                            .into(it)
                    }
                    db.collection("stores").document(user?.storeId!!)
                        .get()
                        .addOnSuccessListener { storeDoc ->
                            val store = storeDoc.toObject(Store::class.java)
                            view?.findViewById<TextView>(R.id.storeName)?.text = store?.storeName
                            avatarClick?.setOnClickListener {
                                val fragment = AccountDetailFragment()
                                val bundle = Bundle().apply {
                                    putParcelable("account", user)
                                    putParcelable("store", store)
                                }
                                fragment.arguments = bundle
                                (activity as AdminMainActivity).supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack("tip_detail_fragment")
                                    .commit()
                            }
                        }
                }

            view?.findViewById<TextView>(R.id.tipName)?.text = nonNullTip.title
            view?.findViewById<ImageView>(R.id.tipStatusIcon)?.setColorFilter(
                when (nonNullTip.approvalStatus) {
                    0 -> resources.getColor(R.color.md_theme_pending, null)
                    1 -> resources.getColor(R.color.md_theme_primary, null)
                    -1 -> resources.getColor(R.color.md_theme_error, null)
                    else -> resources.getColor(R.color.md_theme_primary, null)
                }
            )
            tipStatusTextView?.text = when (nonNullTip.approvalStatus) {
                0 -> "Pending"
                1 -> "Approved"
                -1 -> "Rejected"
                else -> "Unknown"
            }
            tipStatusTextView?.setTextColor(
                when (nonNullTip.approvalStatus) {
                    0 -> resources.getColor(R.color.md_theme_pending, null)
                    1 -> resources.getColor(R.color.md_theme_primary, null)
                    -1 -> resources.getColor(R.color.md_theme_error, null)
                    else -> resources.getColor(R.color.md_theme_primary, null)
                }
            )
            rejectButton?.backgroundTintList = when (nonNullTip.approvalStatus) {
                0 -> resources.getColorStateList(R.color.md_theme_error, null)
                else -> resources.getColorStateList(R.color.md_theme_onPrimary, null)
            }
            rejectButton?.setTextColor(
                when (nonNullTip.approvalStatus) {
                    0 -> resources.getColor(R.color.md_theme_onPrimary, null)
                    else -> resources.getColor(R.color.md_theme_error, null)
                }
            )
            val updateRejectIcon = ContextCompat.getDrawable(
                requireContext(),
                when (nonNullTip.approvalStatus) {
                    0 -> R.drawable.white_red_tick
                    else -> R.drawable.red_tick
                }
            )
            rejectButton?.setCompoundDrawablesWithIntrinsicBounds(updateRejectIcon, null, null, null)

            approveButton?.backgroundTintList = when (nonNullTip.approvalStatus) {
                0 -> resources.getColorStateList(R.color.md_theme_primary, null)
                else -> resources.getColorStateList(R.color.md_theme_onPrimary, null)
            }
            approveButton?.setTextColor(
                when (nonNullTip.approvalStatus) {
                    0 -> resources.getColor(R.color.md_theme_onPrimary, null)
                    else -> resources.getColor(R.color.md_theme_primary, null)
                }
            )
            val updateApproveIcon = ContextCompat.getDrawable(
                requireContext(),
                when (nonNullTip.approvalStatus) {
                    0 -> R.drawable.white_green_tick
                    else -> R.drawable.green_tick
                }
            )
            approveButton?.setCompoundDrawablesWithIntrinsicBounds(updateApproveIcon, null, null, null)

            view?.findViewById<ImageView>(R.id.tipImage)?.let { imageView ->
                Glide.with(this)
                    .load(nonNullTip.imageList[0])
                    .into(imageView)
            }
            view?.findViewById<TextView>(R.id.dateOfTip)?.text = dateFormat(nonNullTip.updatedAt.toString())
            val upvotesText = resources.getString(R.string.upvotes_placeholder, nonNullTip.vote_count)
            view?.findViewById<TextView>(R.id.upvotes)?.text = upvotesText
            view?.findViewById<TextView>(R.id.tipDescription)?.text = nonNullTip.content
        }
    }

    private fun transformParagraph(paragraph: String): String {
        // Replace "**" with "-"
        val transformedParagraph = paragraph.replace("**", "_")

        // Split the paragraph into separate lines
        val lines = transformedParagraph.split(".")

        // Join the lines back together with a "." separator
        return lines.joinToString(".")
    }

    private fun dateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val inputDate = inputFormat.parse(date)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        return outputFormat.format(inputDate)
    }
}
