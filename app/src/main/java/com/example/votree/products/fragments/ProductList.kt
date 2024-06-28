package com.example.votree.products.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.databinding.FragmentProductListBinding
import com.example.votree.products.adapters.ProductAdapter
import com.example.votree.products.adapters.SuggestionSearchAdapter
import com.example.votree.products.models.Product
import com.example.votree.products.view_models.ProductFilterViewModel
import com.example.votree.products.view_models.ProductViewModel
import com.example.votree.users.activities.ChatActivity
import com.example.votree.utils.GridSpacingItemDecoration
import com.example.votree.utils.uiUtils.Companion.calculateNoOfColumns
import com.google.android.material.tabs.TabLayout

class ProductList : Fragment() {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mFirebaseProductViewModel: ProductViewModel
    private val FilterProductViewModel: ProductFilterViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ProductFilterViewModel::class.java)
    }
    private lateinit var productAdapter: ProductAdapter
    private var sortPriceAscending = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        setUpViewModel()
        setUpTabLayout()
        navigateToProductDetail()
        setupSearchBar()
        setupSearchView()
        setupFilterObserver()

        mFirebaseProductViewModel.fetchProductsPerPage(null, pageSize)
    }

    private fun setupSearchBar() {
        binding.searchBar.inflateMenu(R.menu.search_bar)
        binding.searchBar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.cart -> {
                    val action = ProductListDirections.actionProductListToCartList()
                    findNavController().navigate(action)
                    true
                }
                R.id.chat -> {
                    val intent = Intent(context, ChatActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSearchView() {
        val suggestionAdapter = SuggestionSearchAdapter{ suggestion ->
            Log.d("SuggestionSearchFragment", "Suggestion clicked: $suggestion")
            // TODO: Navigate to ResultFragment with the suggestion
            val action = ProductListDirections.actionProductListToSearchResultFragment(suggestion)
            findNavController().navigate(action)
        }
        binding.suggestionsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.suggestionsRecyclerView.adapter = suggestionAdapter

        fun onSearchQueryChanged(query: String, suggestionAdapter: SuggestionSearchAdapter) {
            if (query.isBlank()) return
            mFirebaseProductViewModel.getDebouncedProductSuggestions(query).observe(viewLifecycleOwner) { suggestions ->
                Log.d("SuggestionSearchFragment", "Suggestions: $suggestions")
                suggestionAdapter.submitList(suggestions)
            }
        }

        val searchEditText = binding.searchView.editText
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                onSearchQueryChanged(query, suggestionAdapter)
            }
        })
        searchEditText
            .setOnEditorActionListener { v, actionId, event ->
                if (actionId == 3) {
                    val query = v.text.toString()
                    val action = ProductListDirections.actionProductListToSearchResultFragment(query)
                    findNavController().navigate(action)
                    true
                } else false
            }

    }

    private fun calculateNoOfColumns(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columnWidthDp = 180
        return (screenWidthDp / columnWidthDp).toInt()
    }

    private val pageSize = 5
    private var isLoading = false

    private fun setUpRecyclerView() {
        val numberOfColumns = calculateNoOfColumns(requireContext())
        productAdapter = ProductAdapter()
        binding.productListRv.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), numberOfColumns)
            setHasFixedSize(true)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) {
                        super.onScrolled(recyclerView, dx, dy)

                        val layoutManager = recyclerView.layoutManager as GridLayoutManager
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                            loadMoreData()
                        }
                    }
                }
            })
        }

        binding.productListRv.addItemDecoration(
            GridSpacingItemDecoration(
                numberOfColumns,
                10,
                true
            )
        )
    }

    private fun loadMoreData() {
        if (!isLoading) {
            isLoading = true
            mFirebaseProductViewModel.lastVisibleProduct.value?.let { lastVisible ->
                mFirebaseProductViewModel.fetchProductsPerPage(lastVisible, pageSize)
            }
        }
    }

    private fun setUpViewModel(){
        mFirebaseProductViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        mFirebaseProductViewModel.products.observe(viewLifecycleOwner) { products ->
            isLoading = false
            Log.d("ProductList", "Products: $products")
            productAdapter.setData(products)
        }
    }

    private fun setUpTabLayout()
    {
        binding.sortProductTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> mFirebaseProductViewModel.sortProductsBySoldQuantity()
                    1 -> mFirebaseProductViewModel.sortProductsByCreationDate()
                    2 -> {
                        mFirebaseProductViewModel.sortProductsByPrice(sortPriceAscending)
                        tab.icon = if (sortPriceAscending) {
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_ascending)
                        } else {
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_descending)
                        }
                    }
                    3 -> {
                        showProductFilterBottomSheet()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // If unseleted the 2nd tab, reset the sortPriceAscending to true
                if (tab?.position == 2) {
                    sortPriceAscending = true
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_unfold)
                }else if(tab?.position == 3){
                    resetProductList()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (tab?.position == 2) {
                    sortPriceAscending = !sortPriceAscending
                    mFirebaseProductViewModel.sortProductsByPrice(sortPriceAscending)
                    tab.icon = if (sortPriceAscending) {
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_ascending)
                    } else {
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_descending)
                    }
                }
                else if (tab?.position == 3) {
                    showProductFilterBottomSheet()
                } else {
                    resetProductList()
                }
            }
        })
    }

    private fun showProductFilterBottomSheet() {
        val bottomSheetFragment = ProductFilterBottomSheet()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    private fun navigateToProductDetail(){
        productAdapter.setOnProductClickListener(object : ProductAdapter.OnProductClickListener {
            override fun onProductClick(product: Product) {
                val action = ProductListDirections.actionProductListToProductDetail(product, "user")
                findNavController().navigate(action)
            }
        })
    }

    private fun filterProducts(query: String) {
        if(query.isNotEmpty()) {
            mFirebaseProductViewModel.products.observe(viewLifecycleOwner) { products ->
                val filteredProducts = products.filter { product ->
                    product.productName.contains(query, ignoreCase = true)
                }
                productAdapter.setData(filteredProducts)
            }
        } else {
            resetProductList()
        }
    }

    private fun resetProductList(){
        mFirebaseProductViewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.setData(products)
        }
    }

    private fun setupFilterObserver() {
        FilterProductViewModel.currentFilterCriteria.observe(viewLifecycleOwner) { criteria ->
            applyFiltersToProductList(criteria)
        }
    }

    private fun applyFiltersToProductList(criteria: ProductFilterViewModel.FilterCriteria) {
        // Log the criteria to check if the filter is working correctly
        Log.d("ProductList", "Filter criteria: $criteria")
        mFirebaseProductViewModel.products.observe(viewLifecycleOwner) { products ->
            Log.d("ProductList", "All products: $products")

            // Filter products based on any matching criteria within each category
            val filteredProducts = products.filter { product ->
                val matchesPlantType = criteria.selectedPlantTypes.isEmpty() || criteria.selectedPlantTypes.contains(product.type)
                val matchesClimate = criteria.selectedClimates.isEmpty() || criteria.selectedClimates.contains(product.suitClimate)
                val matchesEnvironment = criteria.selectedEnvironments.isEmpty() || criteria.selectedEnvironments.contains(product.suitEnvironment)

                matchesPlantType && matchesClimate && matchesEnvironment
            }

            Log.d("ProductList", "Filtered products: $filteredProducts")
            productAdapter.setData(filteredProducts)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
