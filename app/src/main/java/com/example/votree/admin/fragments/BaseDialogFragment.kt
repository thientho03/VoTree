package com.example.votree.admin.fragments

import DialogFragmentListener
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

abstract class BaseDialogFragment<T> : DialogFragment(), OnItemClickListener {

    private var listener: DialogFragmentListener? = null
    protected abstract val adapter: BaseListAdapter<T>
    protected abstract val collectionName: String
    protected abstract val dialogTitle: String
    protected abstract val accountIdKey: String

    protected val db = Firebase.firestore

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val rvItems = inflater.inflate(R.layout.fragment_list, null)
        val accountId = arguments?.getString(accountIdKey)
        fetchDataFromFirestore(accountId)

        rvItems.findViewById<RecyclerView>(R.id.listRecycleView).adapter = adapter
        rvItems.findViewById<RecyclerView>(R.id.listRecycleView).layoutManager = LinearLayoutManager(requireContext())

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(rvItems).setTitle(dialogTitle)
                .setPositiveButton("View") { _, _ ->
                    val selectedPosition = adapter.getSelectedPosition()
                    if (selectedPosition >= 0) onItemSelected(adapter.getSelectedPosition())
                }
                .setNegativeButton("Cancel") { _, _ -> }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    protected abstract fun createAdapter(listener: OnItemClickListener): BaseListAdapter<T>

    protected abstract fun fetchDataFromFirestore(id: String?)

    protected open fun onItemSelected(position: Int) { }

    override fun onItemClicked(view: View?, position: Int) {
        adapter.setSelectedPosition(position)
    }

    override fun onTransactionItemClicked(view: View?, position: Int) { }

    override fun onTipItemClicked(view: View?, position: Int) { }

    override fun onAccountItemClicked(view: View?, position: Int) { }

    override fun onReportItemClicked(view: View?, position: Int, processStatus: Boolean) { }

    // Attach the listener when the fragment is attached to the activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DialogFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement DialogFragmentListener")
        }
    }

    // Detach the listener when the fragment is detached from the activity
    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
