package com.example.votree.admin.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.adapters.ReportListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Report
import com.example.votree.models.Tip
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

class ReportListFragment : BaseListFragment<Report>(), OnItemClickListener {

    override val adapter: BaseListAdapter<Report> by lazy { ReportListAdapter(this) }
    override val itemList = mutableListOf<Report>()
    override val collectionName = "reports"
    private var lastVisibleDocument: DocumentSnapshot? = null
    private val pageSize: Long = 11 // Number of items to fetch per page

    private lateinit var userButton: Button
    private lateinit var pendingButton: Button
    private lateinit var approvedButton: Button
    private lateinit var rejectedButton: Button
    private lateinit var allButton: Button
    private var tempList = mutableListOf<Report>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userButton = view.findViewById(R.id.userButton)
        pendingButton = view.findViewById(R.id.pendingButton)
        approvedButton = view.findViewById(R.id.approvedButton)
        rejectedButton = view.findViewById(R.id.rejectedButton)
        allButton = view.findViewById(R.id.allButton)

        userButton.visibility = View.GONE
        pendingButton.text = "Pending"
        approvedButton.text = "Resolved"
        rejectedButton.text = "Unresolved"

        // Set initial button states
        setButtonState(pendingButton, false)
        setButtonState(approvedButton, false)
        setButtonState(rejectedButton, false)
        setButtonState(allButton, true)

        // Set click listeners for the buttons
        pendingButton.setOnClickListener { onButtonClicked(pendingButton) }
        approvedButton.setOnClickListener { onButtonClicked(approvedButton) }
        rejectedButton.setOnClickListener { onButtonClicked(rejectedButton) }
        allButton.setOnClickListener { onButtonClicked(allButton) }
    }

    private fun onButtonClicked(clickedButton: Button) {
        // Reset all buttons to unclicked state
        setButtonState(pendingButton, false)
        setButtonState(approvedButton, false)
        setButtonState(rejectedButton, false)
        setButtonState(allButton, false)

        // Set the clicked button to clicked state
        setButtonState(clickedButton, true)

        // Perform action based on the clicked button
        when (clickedButton) {
            pendingButton -> {
                // Handle pending button click
                tempList = itemList.filter { it.processingState.lowercase() == "pending" || it.processingState.lowercase() == "" }.toMutableList()
                adapter.setData(tempList)
            }
            approvedButton -> {
                // Handle approved button click
                tempList = itemList.filter { it.processingState.lowercase() == "resolved" }.toMutableList()
                adapter.setData(tempList)
            }
            rejectedButton -> {
                tempList = itemList.filter { it.processingState.lowercase() == "unresolved" }.toMutableList()
                adapter.setData(tempList)
            }
            allButton -> {
                tempList = itemList
                adapter.setData(tempList)
            }
        }
    }

    private fun setButtonState(button: Button, isClicked: Boolean) {
        if (isClicked) {
            when (button) {
                pendingButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.yellow, null)
                    button.setTextColor(resources.getColor(R.color.md_theme_onPrimary, null))
                }
                approvedButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.md_theme_primary, null)
                    button.setTextColor(resources.getColor(R.color.md_theme_onPrimary, null))
                }
                rejectedButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.md_theme_error, null)
                    button.setTextColor(resources.getColor(R.color.md_theme_onPrimary, null))
                }
                allButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.colorPrimaryDark, null)
                    button.setTextColor(resources.getColor(R.color.md_theme_onPrimary, null))
                }
            }
        } else {
            when (button) {
                pendingButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.md_theme_onPrimary, null)
                    button.setTextColor(resources.getColor(R.color.yellow, null))
                }
                approvedButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.md_theme_onPrimary, null)
                    button.setTextColor(resources.getColor(R.color.md_theme_primary, null))
                }
                rejectedButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.md_theme_onPrimary, null)
                    button.setTextColor(resources.getColor(R.color.md_theme_error, null))
                }
                allButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.md_theme_onPrimary, null)
                    button.setTextColor(resources.getColor(R.color.colorPrimaryDark, null))
                }
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_list_with_filter

    private var isFetchingData = false

    override fun fetchDataFromFirestore() {
        super.fetchDataFromFirestore()
        if (currentUserId != "") {
            db.collection(collectionName).whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("ReportListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    itemList.clear()
                    for (doc in snapshots!!) {
                        val report = doc.toObject(Report::class.java)
                        report.id = doc.id
                        itemList.add(report)
                    }
                    adapter.setData(itemList)
                }

            db.collection(collectionName).whereEqualTo("reporterId", currentUserId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("ReportListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    for (doc in snapshots!!) {
                        val report = doc.toObject(Report::class.java)
                        report.id = doc.id
                        if (report.reporterId != report.userId) {
                            itemList.add(report)
                        }
                    }

                    adapter.setData(itemList)
                }
        }
        else {
            db.collection(collectionName)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("ReportListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    itemList.clear()
                    tempList.clear()
                    for (doc in snapshots!!) {
                        val report = doc.toObject(Report::class.java)
                        report.id = doc.id
                        itemList.add(report)
                    }
                    tempList = itemList
                    adapter.setData(itemList)
                }
        }
    }


    override fun onReportItemClicked(view: View?, position: Int, processStatus: Boolean) {
        (activity as AdminMainActivity).onReportItemClicked(view, position, processStatus)
        val topAppBar: MaterialToolbar = (activity as AdminMainActivity).findViewById(R.id.topAppBar)
        topAppBar.menu.findItem(R.id.more).title = "Delete Report"
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.more -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Report")
                        .setMessage("Are you sure you want to delete this report?")
                        .setPositiveButton("Yes") { _, _ ->
                            db.collection(collectionName).document(adapter.getItem(position).id).delete()
                                .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!") }
                                .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error deleting document", e) }

                            (activity as FragmentActivity).supportFragmentManager.popBackStack()
                        }
                        .setNegativeButton("No") { _, _ -> }
                        .show()
                    true
                }

                else -> false
            }
        }

        val fragment = ReportDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("report", adapter.getItem(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("report_list_fragment").commit()
    }

    override fun searchItem(query: String) {
        val searchResult = tempList.filter {
            val user = getUserById(it.reporterId)
            user.username.contains(query, ignoreCase = true) || it.shortDescription.contains(query, ignoreCase = true)
        }

        adapter.setData(searchResult)
    }
}
