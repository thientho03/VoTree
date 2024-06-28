package com.example.votree.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

abstract class BaseListFragment<T> : Fragment(), OnItemClickListener {
    val userList = mutableListOf<User>()

    protected abstract val adapter: BaseListAdapter<T>
    protected abstract val itemList: MutableList<T>
    protected abstract val collectionName: String
    protected var currentUserId: String = ""
    protected val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)

        val recyclerView: RecyclerView? = view?.findViewById(R.id.listRecycleView)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context)

        fetchDataFromFirestore()

        return view
    }

    protected abstract fun getLayoutId(): Int

    protected open fun fetchDataFromFirestore() {
        db.collection("users").addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            userList.clear()
            for (doc in snapshots!!) {
                val user = doc.toObject(User::class.java)
                userList.add(user)
            }
        }
    }

    override fun onItemClicked(view: View?, position: Int) {}

    override fun onProductItemClicked(view: View?, position: Int) {}

    override fun onTransactionItemClicked(view: View?, position: Int) {}

    override fun onTipItemClicked(view: View?, position: Int) {}

    override fun onAccountItemClicked(view: View?, position: Int) {}

    override fun onReportItemClicked(view: View?, position: Int, processStatus: Boolean) {}

    override fun searchItem(query: String) {
        val searchResult = itemList.filter {
            it.toString().contains(query, ignoreCase = true)
        }

        adapter.setData(searchResult)
    }

    protected fun getUserById(userId: String): User {
        return userList.find { it.id == userId } as User
    }

    fun setUserId(currentUserId: String) {
        this.currentUserId = currentUserId
    }
}