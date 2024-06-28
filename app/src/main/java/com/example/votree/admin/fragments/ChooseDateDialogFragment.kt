package com.example.votree.admin.fragments

import DialogFragmentListener
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.admin.adapters.DialogDateListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ChooseDateDialogFragment : DialogFragment(), OnItemClickListener {

    private var listener: DialogFragmentListener? = null
    private lateinit var adapter: DialogDateListAdapter

    companion object {
        private const val REPORTER_ID = "reporter_id"
        fun newInstance(reporterId: String): ChooseDateDialogFragment {
            val fragment = ChooseDateDialogFragment()
            val args = Bundle()
            args.putString(REPORTER_ID, reporterId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dates = listOf("3 days", "1 week", "30 days", "365 days", "Forever")
        val days = listOf(3, 7, 30, 365, 77777)
        val inflater = requireActivity().layoutInflater
        val rvDates = inflater.inflate(R.layout.fragment_recycleview_date, null)
        val reporterId = arguments?.getString(REPORTER_ID)

        adapter = DialogDateListAdapter(dates, this)
        adapter.setData(dates)

        rvDates.findViewById<RecyclerView>(R.id.dateRecyclerViewFragment).adapter = adapter
        rvDates.findViewById<RecyclerView>(R.id.dateRecyclerViewFragment).layoutManager = LinearLayoutManager(requireContext())

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ban Duration")
            .setView(rvDates)
            .setPositiveButton("Save") { _, _ ->
                val singleItemSelectionPosition = adapter.getSelectedPosition()
                listener?.updateExpireBanDateToFirestore(days[singleItemSelectionPosition], reporterId!!)
                Toast.makeText(context, "Ban successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }


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

    override fun onItemClicked(view: View?, position: Int) {
        adapter.setSelectedPosition(position)
    }

    override fun onProductItemClicked(view: View?, position: Int) {}

    override fun onTransactionItemClicked(view: View?, position: Int) {}

    override fun onTipItemClicked(view: View?, position: Int) {}

    override fun onAccountItemClicked(view: View?, position: Int) {}

    override fun onReportItemClicked(view: View?, position: Int, processStatus: Boolean) {}

    override fun searchItem(query: String) {}
}