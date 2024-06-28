package com.example.votree.admin.fragments

import android.content.ContentValues.TAG
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.AccountListAdapter
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.models.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.Query


class AccountListFragment : BaseListFragment<User>() {

    override val adapter: BaseListAdapter<User> by lazy { AccountListAdapter(this) }
    override val itemList = mutableListOf<User>()
    override val collectionName = "users"
//    private var lastVisibleDocument: DocumentSnapshot? = null
//    private val pageSize: Long = 11 // Number of items to fetch per page
//    private var currentQueryString: String? = null

    private lateinit var userButton: Button
    private lateinit var pendingButton: Button
    private lateinit var storeButton: Button
    private lateinit var rejectedButton: Button
    private lateinit var allButton: Button
    private var tempList = mutableListOf<User>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userButton = view.findViewById(R.id.userButton)
        pendingButton = view.findViewById(R.id.pendingButton)
        storeButton = view.findViewById(R.id.approvedButton)
        rejectedButton = view.findViewById(R.id.rejectedButton)
        allButton = view.findViewById(R.id.allButton)

        rejectedButton.visibility = View.GONE
        pendingButton.visibility = View.GONE
        storeButton.text = "Store"

        // Set initial button states
        setButtonState(userButton, false)
        setButtonState(storeButton, false)
        setButtonState(allButton, true)

        // Set click listeners for the buttons
        userButton.setOnClickListener { onButtonClicked(userButton) }
        storeButton.setOnClickListener { onButtonClicked(storeButton) }
        allButton.setOnClickListener { onButtonClicked(allButton) }
    }

    private fun onButtonClicked(clickedButton: Button) {
        // Reset all buttons to unclicked state
        setButtonState(userButton, false)
        setButtonState(storeButton, false)
        setButtonState(rejectedButton, false)
        setButtonState(allButton, false)

        // Set the clicked button to clicked state
        setButtonState(clickedButton, true)

        // Perform action based on the clicked button
        when (clickedButton) {
            userButton -> {
                // Handle pending button click
                tempList = itemList.filter { it.role.lowercase() == "user" }.toMutableList()
                adapter.setData(tempList)
            }
            storeButton -> {
                // Handle approved button click
                tempList = itemList.filter { it.role.lowercase() == "store" }.toMutableList()
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
                userButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.md_theme_primary, null)
                    button.setTextColor(resources.getColor(R.color.md_theme_onPrimary, null))
                }
                storeButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.md_theme_primary, null)
                    button.setTextColor(resources.getColor(R.color.md_theme_onPrimary, null))
                }
                allButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.colorPrimaryDark, null)
                    button.setTextColor(resources.getColor(R.color.md_theme_onPrimary, null))
                }
            }
        } else {
            when (button) {
                userButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.md_theme_onPrimary, null)
                    button.setTextColor(resources.getColor(R.color.md_theme_primary, null))
                }
                storeButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.md_theme_onPrimary, null)
                    button.setTextColor(resources.getColor(R.color.md_theme_primary, null))
                }
                allButton -> {
                    button.backgroundTintList = resources.getColorStateList(R.color.md_theme_onPrimary, null)
                    button.setTextColor(resources.getColor(R.color.colorPrimaryDark, null))
                }
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_list_with_filter

//    private var isFetchingData = false

    override fun fetchDataFromFirestore() {
        db.collection(collectionName).orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AccountListFragment", "listen:error", e)
                    return@addSnapshotListener
                }

                itemList.clear()
                tempList.clear()
                for (doc in snapshots!!) {
                    val account = doc.toObject(User::class.java)
                    account.id = doc.id
                    itemList.add(account)
                }
                tempList.addAll(itemList)
                adapter.setData(itemList)
            }
    }

    override fun onAccountItemClicked(view: View?, position: Int) {
        (activity as AdminMainActivity).onAccountItemClicked(view, position)
        val topAppBar: MaterialToolbar = (activity as AdminMainActivity).findViewById(R.id.topAppBar)
        topAppBar.menu.findItem(R.id.more).title = "Delete Account"
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.more -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete this account?")
                        .setPositiveButton("Yes") { _, _ ->
                            db.collection(collectionName).document(adapter.getItem(position).id).delete()
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

                            (activity as FragmentActivity).supportFragmentManager.popBackStack()
                        }
                        .setNegativeButton("No") { _, _ -> }
                        .show()
                    true
                }

                else -> false
            }
        }

        val fragment = AccountDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("account", adapter.getItem(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("account_list_fragment").commit()
    }

    override fun searchItem(query: String) {
        val searchResult = tempList.filter {
            it.username.contains(query, ignoreCase = true) || it.role.contains(query, ignoreCase = true)
        }

        adapter.setData(searchResult)
    }
}
