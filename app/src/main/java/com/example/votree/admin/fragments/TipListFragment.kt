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
import com.example.votree.admin.adapters.TipListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Tip
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

class TipListFragment : BaseListFragment<Tip>(), OnItemClickListener {

    override val adapter: BaseListAdapter<Tip> by lazy { TipListAdapter(this) }
    override val itemList = mutableListOf<Tip>()
    override val collectionName = "ProductTip"
    private var lastVisibleDocument: DocumentSnapshot? = null
    private val pageSize: Long = 11 // Number of items to fetch per page

    private lateinit var userButton: Button
    private lateinit var pendingButton: Button
    private lateinit var approvedButton: Button
    private lateinit var rejectedButton: Button
    private lateinit var allButton: Button
    private var tempList = mutableListOf<Tip>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userButton = view.findViewById(R.id.userButton)
        pendingButton = view.findViewById(R.id.pendingButton)
        approvedButton = view.findViewById(R.id.approvedButton)
        rejectedButton = view.findViewById(R.id.rejectedButton)
        allButton = view.findViewById(R.id.allButton)

        userButton.visibility = View.GONE

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
                tempList = itemList.filter { it.approvalStatus == 0 }.toMutableList()
                adapter.setData(tempList)
            }
            approvedButton -> {
                // Handle approved button click
                tempList = itemList.filter { it.approvalStatus == 1 }.toMutableList()
                adapter.setData(tempList)
            }
            rejectedButton -> {
                tempList = itemList.filter { it.approvalStatus == -1 }.toMutableList()
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

    private var isFetchingData = false // Flag to prevent concurrent fetch operations

    public override fun fetchDataFromFirestore() {
        super.fetchDataFromFirestore()
        if (currentUserId != "") {
            db.collection(collectionName)
                .whereEqualTo("userId", currentUserId)
                .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("TipListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    itemList.clear()
                    for (doc in snapshots!!) {
                        val tip = doc.toObject(Tip::class.java)
                        tip.id = doc.id
                        itemList.add(tip)
                    }
                    adapter.setData(itemList)
                }
        } else {
            db.collection(collectionName)
                .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("TipListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    itemList.clear()
                    tempList.clear()
                    for (doc in snapshots!!) {
                        val tip = doc.toObject(Tip::class.java)
                        tip.id = doc.id
                        itemList.add(tip)
                    }
                    tempList.addAll(itemList)
                    adapter.setData(itemList)
                }
        }
    }

//    public override fun fetchDataFromFirestore() {
//        super.fetchDataFromFirestore()
//
//        val query = db.collection(collectionName)
//            .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
//
//        if (currentUserId.isNotEmpty()) {
//            query.whereEqualTo("userId", currentUserId)
//        }
//
//        query.addSnapshotListener { snapshots, e ->
//            if (e != null) {
//                Log.w("TipListFragment", "listen:error", e)
//                return@addSnapshotListener
//            }
//
//            val tempList = mutableListOf<Tip>()
//            snapshots?.forEach { doc ->
//                val tip = doc.toObject(Tip::class.java)
//                tip.id = doc.id
//                tempList.add(tip)
//            }
//
//            adapter.setData(tempList)
//        }
//    }


//    override fun fetchDataFromFirestore(queryString: String?) {
//        if (isFetchingData) {
//            // Avoid concurrent fetch operations
//            return
//        }
//        isFetchingData = true
//
//        var query = db.collection(collectionName)
//            .orderBy("updatedAt", Query.Direction.DESCENDING)
//
//        if (currentUserId != "") {
//            query = query.whereEqualTo("userId", currentUserId)
//        }
//
//        // If there is a last visible document, query documents after it
//        if (lastVisibleDocument != null) {
//            query = query.startAfter(lastVisibleDocument!!)
//        }
//
//        query.limit(pageSize)
//            .get()
//            .addOnSuccessListener { documents ->
//                if (!documents.isEmpty) {
//                    lastVisibleDocument = documents.documents[documents.size() - 1] // Update last visible document
//                }
//
//                val newItems = mutableListOf<Tip>()
//                for (doc in documents) {
//                    val tip = doc.toObject(Tip::class.java)
//                    tip.id = doc.id
//                    // Check if the item already exists in the list before adding it
//                    if (!itemList.contains(tip)) {
//                        newItems.add(tip)
//                    }
//                }
//
//                itemList.addAll(newItems)
//                adapter.setData(itemList)
//                isFetchingData = false
//            }
//            .addOnFailureListener { e ->
//                Log.e("TipListFragment", "Error fetching documents: $e")
//                isFetchingData = false
//            }
//    }


    override fun onTipItemClicked(view: View?, position: Int) {
        (activity as AdminMainActivity).onTipItemClicked(view, position)
        val tipIdDelete: String = adapter.getItem(position).id
        val topAppBar: MaterialToolbar = (activity as AdminMainActivity).findViewById(R.id.topAppBar)
        topAppBar.menu.findItem(R.id.more).title = "Delete Tip"
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.more -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Tip")
                        .setMessage("Are you sure you want to delete this tip?")
                        .setPositiveButton("Yes") { _, _ ->
                            db.collection(collectionName).document(tipIdDelete).delete()
                                .addOnSuccessListener {
                                    db.collection("checkContent").whereEqualTo("tipId", tipIdDelete).get().addOnSuccessListener { documents ->
                                        for (doc in documents) {
                                            db.collection("checkContent").document(doc.id).delete()
                                        }
                                    }
                                    Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!")
                                }
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

        val fragment = TipDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("tip", adapter.getItem(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("tip_list_fragment").commit()
    }

    override fun searchItem(query: String) {
        val searchResult: MutableList<Tip> = mutableListOf()

        // Filter tips by title or short description
        searchResult.addAll(tempList.filter {
            it.title.contains(query, ignoreCase = true) || it.shortDescription.contains(query, ignoreCase = true)
        })

        // Filter users by username
        val userSearchResult = userList.filter {
            it.username.contains(query, ignoreCase = true)
        }

        // Add tips associated with the found users
        for (user in userSearchResult) {
            val tip = tempList.find { it.userId == user.id }
            if (tip != null) {
                searchResult.add(tip)
            }
        }

        adapter.setData(searchResult)
    }

}
