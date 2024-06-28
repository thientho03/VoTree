package com.example.votree.products.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.databinding.FragmentAllOrdersBinding
import com.example.votree.products.view_models.OrderHistoryViewModel
import com.example.votree.users.adapters.OrderItemAdapter

class AllOrdersFragment(private val viewModel: OrderHistoryViewModel) : Fragment() {
    private var _binding: FragmentAllOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: OrderItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllOrdersBinding.inflate(inflater, container, false)

        adapter = OrderItemAdapter(emptyList(), viewLifecycleOwner.lifecycleScope)
        binding.orderHistoryRv.layoutManager = LinearLayoutManager(requireContext())
        binding.orderHistoryRv.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
//            adapter.updateTransactions(transactions ?: emptyList())
            (binding.orderHistoryRv.adapter as OrderItemAdapter).apply {
                this.transactions = transactions ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}