package com.example.votree.products.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.databinding.FragmentDeliveredOrdersBinding
import com.example.votree.products.view_models.OrderHistoryViewModel
import com.example.votree.users.adapters.OrderItemAdapter

class DeliveredOrdersFragment(private val viewModel: OrderHistoryViewModel) : Fragment() {
    private var _binding: FragmentDeliveredOrdersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeliveredOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = OrderItemAdapter(emptyList(), viewLifecycleOwner.lifecycleScope)
        binding.orderHistoryRv.layoutManager = LinearLayoutManager(requireContext())
        binding.orderHistoryRv.adapter = adapter

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val deliveredTransactions = transactions?.filter { it.status == "delivered" }
            (binding.orderHistoryRv.adapter as OrderItemAdapter).apply {
                this.transactions = deliveredTransactions ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}