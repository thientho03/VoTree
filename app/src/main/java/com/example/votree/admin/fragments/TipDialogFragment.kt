package com.example.votree.admin.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.adapters.TipListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Tip

class TipDialogFragment : BaseDialogFragment<Tip>() {

    override val adapter: BaseListAdapter<Tip> by lazy { createAdapter(this) }
    override val collectionName: String = "ProductTip"
    override val dialogTitle: String = "List of Tips"
    override val accountIdKey: String = "account_id"

    companion object {
        private const val ACCOUNT_ID = "account_id"
        fun newInstance(accountId: String): TipDialogFragment {
            val fragment = TipDialogFragment()
            val args = Bundle()
            args.putString(ACCOUNT_ID, accountId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun createAdapter(listener: OnItemClickListener): BaseListAdapter<Tip> {
        return TipListAdapter(listener, true)
    }

    override fun fetchDataFromFirestore(id: String?) {
        val tipList = mutableListOf<Tip>()
        db.collection(collectionName).whereEqualTo("userId", id)
            .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("TipListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                tipList.clear()
                for (doc in snapshots!!) {
                    val tip = doc.toObject(Tip::class.java)
                    tip.id = doc.id
                    tipList.add(tip)
                }
                adapter.setData(tipList)
            }
    }

    override fun onItemSelected(position: Int) {
        onTipItemClicked(null, adapter.getSelectedPosition())
    }

    override fun onTipItemClicked(view: View?, position: Int) {
        (activity as AdminMainActivity).onTipItemClicked(view, position)

        val fragment = TipDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("tip", adapter.getItem(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("tip_list_fragment").commit()
    }

    override fun onProductItemClicked(view: View?, position: Int) {}
}