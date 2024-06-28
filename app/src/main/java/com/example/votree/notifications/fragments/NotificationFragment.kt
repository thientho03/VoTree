package com.example.votree.notifications.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.votree.databinding.FragmentNotificationBinding
import com.example.votree.notifications.adapters.NotificationAdapter
import com.example.votree.notifications.models.Notification
import com.example.votree.notifications.view_models.NotificationViewModel
import com.example.votree.products.repositories.TransactionRepository
import com.example.votree.users.activities.OrderHistoryActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationFragment : Fragment(), NotificationAdapter.OnNotificationClickListener {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()
    private var adapter = NotificationAdapter(this)
    private var selectedTab = 0
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchNotifications()
        }
        setupRecyclerView()
        observeNotifications()
    }

    private fun setupRecyclerView() {
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.notificationRecyclerView.adapter = adapter

        binding.sortNotificationTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTab = tab?.position ?: 0
                observeNotifications()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                onTabSelected(tab)
            }
        })
    }

    override fun onNotificationClick(notification: Notification) {
        Log.d("NotificationFragment", "Notification clicked: $notification")
        if (!notification.read) {
            viewModel.updateNotificationReadStatus(notification.id, true)
        }

        // If notification.title == "New Order", navigate to OrderManagementForStoreFragment
        if (notification.title == "New Order") {
            CoroutineScope(Dispatchers.IO).launch {
                val transactionRepository = TransactionRepository(FirebaseFirestore.getInstance())
                val transaction = transactionRepository.getTransaction(notification.orderId)
                withContext(Dispatchers.Main) {
                    Log.d("NotificationFragment", "Transaction: $transaction")
                    // Navigate to OrderManagementForStoreFragment
                    val action = NotificationFragmentDirections.actionNotificationsFragmentToOrderDetailsFragment(transaction)
                    findNavController().navigate(action)
                }
            }
        }
        else if (notification.title == "Order Delivered" || notification.title == "Order Denied") {
            // start intent OrderHistoryActivity
            val intent = Intent(context, OrderHistoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeNotifications() {
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            val filteredNotifications = when (selectedTab) {
                0 -> notifications
                1 -> notifications.filter { it.read }
                2 -> notifications.filter { !it.read }
                else -> notifications
            }
            Log.d("NotificationFragment", "Filtered notifications: $filteredNotifications")
            (binding.notificationRecyclerView.adapter as NotificationAdapter).submitList(
                filteredNotifications
            )
            swipeRefreshLayout.isRefreshing = false
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
