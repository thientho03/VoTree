package com.example.votree.admin.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.adapters.TransactionListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Store
import com.example.votree.models.Transaction
import com.example.votree.models.User

class TransactionDialogFragment : BaseDialogFragment<Transaction>() {

    override val adapter: BaseListAdapter<Transaction> by lazy { createAdapter(this) }
    override val collectionName: String = "transactions"
    override val dialogTitle: String = "List of Transactions"
    override val accountIdKey: String = "account_id"

    companion object {
        private const val ACCOUNT_ID = "account_id"
        fun newInstance(accountId: String): TransactionDialogFragment {
            val fragment = TransactionDialogFragment()
            val args = Bundle()
            args.putString(ACCOUNT_ID, accountId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun createAdapter(listener: OnItemClickListener): BaseListAdapter<Transaction> {
        return TransactionListAdapter(listener, true)
    }

    override fun fetchDataFromFirestore(id: String?) {
        val transactionList = mutableListOf<Transaction>()
        transactionList.clear()

        db.collection("users").whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val user = doc.toObject(User::class.java)
                    for (transactionId in user.transactionIdList) {
                        db.collection(collectionName).whereEqualTo("id", transactionId)
                            .get()
                            .addOnSuccessListener { transactions ->
                                for (transactionDoc in transactions) {
                                    val transaction =
                                        transactionDoc.toObject(Transaction::class.java)
                                    transactionList.add(transaction)
                                    if (transactionList.size == user.transactionIdList.size) {
                                        adapter.setData(transactionList.sortedByDescending { it.createdAt })
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("TransactionListActivity", "Error fetching transactions", e)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("TransactionListActivity", "Error fetching user details", e)
            }

        db.collection("stores").whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val store = doc.toObject(Store::class.java)
                    for (transactionId in store.transactionIdList) {
                        db.collection(collectionName).whereEqualTo("id", transactionId)
                            .get()
                            .addOnSuccessListener { transactions ->
                                for (transactionDoc in transactions) {
                                    val transaction =
                                        transactionDoc.toObject(Transaction::class.java)
                                    transactionList.add(transaction)
                                    Log.d("transactionList.size", "transactionList.size: ${transactionList.size}")
                                    Log.d("store.transactionIdList.size", "store.transactionIdList.size: ${store.transactionIdList.size}")
                                    if (transactionList.size == store.transactionIdList.size) {
                                        Log.d("YES", "YES")
                                        adapter.setData(transactionList.sortedByDescending { it.createdAt })
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("TransactionListActivity", "Error fetching transactions", e)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("TransactionListActivity", "Error fetching user details", e)
            }
    }

    override fun onItemSelected(position: Int) {
        onTransactionItemClicked(null, adapter.getSelectedPosition())
    }

    override fun onTransactionItemClicked(view: View?, position: Int) {
        (activity as AdminMainActivity).onTransactionItemClicked(view, position)
        val fragment = TransactionDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("transaction", adapter.getItem(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("transaction_list_fragment").commit()
    }

    override fun onProductItemClicked(view: View?, position: Int) {}
}