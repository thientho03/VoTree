package com.example.votree.products.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.votree.databinding.FragmentSearchResultBinding
import com.example.votree.products.adapters.ProductAdapter
import com.example.votree.products.models.Product
import com.example.votree.products.view_models.ProductViewModel
import com.example.votree.utils.GridSpacingItemDecoration

class SearchResultFragment : Fragment() {
    lateinit var binding: FragmentSearchResultBinding
    val viewModel : ProductViewModel by viewModels()
    lateinit var productAdapter : ProductAdapter
    val args : SearchResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        setupToolbar()
        Log.d("SearchResultFragment", "Query: ${args.query}")
        viewModel.searchProducts(args.query).observe(viewLifecycleOwner) {
            Log.d("SearchResultFragment", "Products: $it")
            productAdapter.setData(it)
        }
    }

    private fun setupToolbar(){
        binding.searchResultToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun calculateNoOfColumns(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columnWidthDp = 180 // Assume each item in the grid takes up 180dp
        return (screenWidthDp / columnWidthDp).toInt()
    }

    private fun setUpRecyclerView(){
        val numberOfColumns = calculateNoOfColumns(requireContext())
        productAdapter = ProductAdapter()

        binding.searchResultRv.adapter = productAdapter
        binding.searchResultRv.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)
        binding.searchResultRv.addItemDecoration(
            GridSpacingItemDecoration(
                numberOfColumns,
                10,
                true
            )
        )
        productAdapter.setOnProductClickListener(object : ProductAdapter.OnProductClickListener {
            override fun onProductClick(product: Product) {
                val action = SearchResultFragmentDirections.actionSearchResultFragmentToProductDetail(product, "user")
                findNavController().navigate(action)
            }
        })
    }
}