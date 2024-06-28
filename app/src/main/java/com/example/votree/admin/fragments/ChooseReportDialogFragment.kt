package com.example.votree.admin.fragments

import DialogFragmentListener
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.admin.interfaces.OnItemClickListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.ReportListAdapter
import com.example.votree.models.Report

@Suppress("DEPRECATION")
class ChooseReportDialogFragment : DialogFragment(), OnItemClickListener {

    private var listener: DialogFragmentListener? = null
    private val adapter: ReportListAdapter by lazy { ReportListAdapter(this, true) }
    private val db = Firebase.firestore

    companion object {
        private const val ACCOUNT_ID = "account_id"
        fun newInstance(accountId: String): ChooseReportDialogFragment {
            val fragment = ChooseReportDialogFragment()
            val args = Bundle()
            args.putString(ACCOUNT_ID, accountId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstancState: Bundle?) : Dialog {
        val inflater = requireActivity().layoutInflater
        val rvReports = inflater.inflate(R.layout.fragment_list, null)
        val accountId = arguments?.getString(ACCOUNT_ID)
        ReportListFragment().setUserId(accountId ?: ".")
        val reportList = mutableListOf<Report>()
        db.collection("reports").whereEqualTo("userId", accountId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ReportListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                reportList.clear()
                for (doc in snapshots!!) {
                    val report = doc.toObject(Report::class.java)
                    report.id = doc.id
                    reportList.add(report)
                }
                adapter.setData(reportList)
            }

        db.collection("reports").whereEqualTo("reporterId", accountId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ReportListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                for (doc in snapshots!!) {
                    val report = doc.toObject(Report::class.java)
                    report.id = doc.id
                    if (report.reporterId != report.userId) {
                        reportList.add(report)
                    }
                }

                adapter.setData(reportList)
            }

        rvReports.findViewById<RecyclerView>(R.id.listRecycleView).adapter = adapter
        rvReports.findViewById<RecyclerView>(R.id.listRecycleView).layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(rvReports).setTitle("List of Reports")
                .setPositiveButton("View") { _, _ ->
                    val currentReport = adapter.getItem(adapter.getSelectedPosition())
                    onReportItemClicked(null, adapter.getSelectedPosition(), currentReport.processStatus)
                }
                .setNegativeButton("Cancel") { _, _ -> }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
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

    override fun onTipItemClicked(view: View?, position: Int) { }

    override fun onAccountItemClicked(view: View?, position: Int) { }

    override fun onReportItemClicked(view: View?, position: Int, processStatus: Boolean) {
        val topAppBar = (activity as AdminMainActivity).findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        val report: Report = adapter.getItem(position)
        topAppBar.title = report.shortDescription
        when (processStatus) {
            true -> topAppBar.setTitleTextColor(resources.getColor(R.color.md_theme_primary))
            false -> topAppBar.setTitleTextColor(resources.getColor(R.color.md_theme_error))
        }
        val fragment = ReportDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("report", report)
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("account_detail_fragment").commit()
    }

    override fun searchItem(query: String) { }
}