package com.example.votree.users.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.R
import com.example.votree.databinding.FragmentOrderManagementForStoreBinding
import com.example.votree.products.models.Transaction
import com.example.votree.products.repositories.TransactionRepository
import com.example.votree.users.adapters.OrderManagementAdapter
import com.example.votree.users.repositories.UserRepository
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderManagementForStoreFragment : Fragment() {
    private lateinit var binding: FragmentOrderManagementForStoreBinding
    private val transactionRepository = TransactionRepository(FirebaseFirestore.getInstance())
    private lateinit var coroutineScope: CoroutineScope
    private var orders: List<Transaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderManagementForStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the tab layout
        binding.orderFilterTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                filterOrders(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Fetch the orders and set up the adapter
        coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch(Dispatchers.IO) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val user = UserRepository(FirebaseFirestore.getInstance()).getUser(userId)
            val storeId = user?.storeId ?: ""

            orders = transactionRepository.fetchOrdersForStore(storeId)
            withContext(Dispatchers.Main) {
                Log.d("OrderManagementForStoreFragment", "Orders: $orders")
                setupOrderManagementAdapter(orders)
            }
        }

        // Set the navigation icon to the back button
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.arrow_back_24px)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.toolbar.title = getString(R.string.order_management)
    }

    private fun setupOrderManagementAdapter(orders: List<Transaction>) {
        val adapter = OrderManagementAdapter(orders, layoutInflater)
        adapter.setOnItemClickListener(object : OrderManagementAdapter.OnItemClickListener {
            override fun onItemClick(transaction: Transaction) {
                navigateToOrderDetails(transaction)
            }
        })
        binding.orderManagementRv.layoutManager = LinearLayoutManager(requireContext())
        binding.orderManagementRv.adapter = adapter
    }

    private fun filterOrders(position: Int) {
        val filteredOrders = when (position) {
            0 -> orders
            1 -> orders.filter { it.status == "pending" }
            2 -> orders.filter { it.status == "delivered" }
            3 -> orders.filter { it.status == "denied" }
            else -> orders
        }
        setupOrderManagementAdapter(filteredOrders)
    }

    private fun navigateToOrderDetails(transaction: Transaction) {
        val action =
            OrderManagementForStoreFragmentDirections.actionOrderManagementForStoreFragmentToOrderDetailsFragment(
                transaction
            )
        findNavController().navigate(action)
    }

    private fun onBackPressed() {
        // navigate back
        findNavController().popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}