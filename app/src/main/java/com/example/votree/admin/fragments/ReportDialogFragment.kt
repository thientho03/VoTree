package com.example.votree.admin.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.adapters.ReportListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Report
import com.google.firebase.firestore.Query

class ReportDialogFragment : BaseDialogFragment<Report>() {

    override val adapter: BaseListAdapter<Report> by lazy { createAdapter(this) }
    override val collectionName: String = "reports"
    override val dialogTitle: String = "List of Reports"
    override val accountIdKey: String = "account_id"

    companion object {
        private const val ACCOUNT_ID = "account_id"
        fun newInstance(userId: String): ReportDialogFragment {
            val fragment = ReportDialogFragment()
            val args = Bundle()
            args.putString(ACCOUNT_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun createAdapter(listener: OnItemClickListener): BaseListAdapter<Report> {
        return ReportListAdapter(listener, true)
    }

    override fun fetchDataFromFirestore(id: String?) {
        val reportList = mutableListOf<Report>()
        db.collection(collectionName).whereEqualTo("userId", id)
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

        db.collection(collectionName).whereEqualTo("reporterId", id)
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

                adapter.setData(reportList.sortedByDescending { it.createdAt })
            }
    }

    override fun onItemSelected(position: Int) {
        val currentReport = adapter.getItem(adapter.getSelectedPosition())
        onReportItemClicked(null, adapter.getSelectedPosition(), currentReport.processStatus)
    }

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

    override fun onProductItemClicked(view: View?, position: Int) {}
}