package com.example.votree.products.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.R
import com.example.votree.databinding.FragmentPointTransactionBinding
import com.example.votree.products.adapters.PointTransactionAdapter
import com.example.votree.products.repositories.PointTransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PointTransactionFragment : Fragment() {

    private var _binding: FragmentPointTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: PointTransactionRepository
    private lateinit var adapter: PointTransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPointTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = PointTransactionRepository()
        setupRecyclerView()
        setupView()
        loadPointTransactions()
        setupNavigation()
    }

    private fun setupNavigation() {
        // Set the navigation icon to the back button
        binding.toolbar.navigationIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.arrow_back_24px)
        binding.toolbar.title = getString(R.string.user_profile_setting)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = PointTransactionAdapter(listOf())
        binding.pointTransactionRv.layoutManager = LinearLayoutManager(context)
        binding.pointTransactionRv.adapter = adapter
    }

    private fun setupView() {
        val pointTransactionRepository = PointTransactionRepository()
        lifecycleScope.launch {
            val currentPoints = pointTransactionRepository.getCurrentPoints()
            binding.pointsTv.text = currentPoints.toString()
        }
    }

    private fun loadPointTransactions() {
        CoroutineScope(Dispatchers.IO).launch {
            val transactions = repository.getPointTransactionsForUser()
            CoroutineScope(Dispatchers.Main).launch {
                adapter = PointTransactionAdapter(transactions)
                binding.pointTransactionRv.adapter = adapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
