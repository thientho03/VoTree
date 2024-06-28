package com.example.votree.products.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.R
import com.example.votree.databinding.FragmentProductReviewListBinding
import com.example.votree.products.adapters.UserReviewAdapter
import com.example.votree.products.factories.ProductReviewListViewModelFactory
import com.example.votree.products.repositories.ReviewRepository
import com.example.votree.products.view_models.ProductReviewListViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


interface OnRatingSelectedListener {
    fun onRatingSelected(ratings: Set<Float>)
}

class ProductReviewListFragment : Fragment(), OnRatingSelectedListener {
    private var _binding: FragmentProductReviewListBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<ProductReviewListFragmentArgs>()
    val reviewRepository = ReviewRepository()
    private val viewModel: ProductReviewListViewModel by viewModels {
        ProductReviewListViewModelFactory(reviewRepository, args.currentProduct.id)
    }
    private lateinit var userReviewAdapter: UserReviewAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductReviewListBinding.inflate(inflater, container, false)

        setFragmentResultListener("requestKey") { key, bundle ->
            val ratings = bundle.getFloatArray("selectedRatings")
            if (ratings != null) {
                onRatingSelected(ratings.toSet())
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        setupTabLayout()
        observeViewModel()
        setupNavigation()
    }

    private fun setupNavigation() {
        binding.toolbar.navigationIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.arrow_back_24px)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.title = getString(R.string.product_reviews)
    }

    private fun setUpRecyclerView() {
        userReviewAdapter = UserReviewAdapter(emptyList(), coroutineScope)
        binding.productReviewListRv.apply {
            adapter = userReviewAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        // Observe the filtered reviews LiveData from the ViewModel
        viewModel.filteredReviews.observe(viewLifecycleOwner) { reviews ->
            Log.d("ProductReviewListFragment", "Filtered reviews: $reviews")
            userReviewAdapter.userReviews = reviews
            userReviewAdapter.notifyDataSetChanged()
        }
    }

    private fun setupTabLayout() {
        binding.sortReviewTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.text == "Star") {
                    showReviewFilterBottomSheet()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselection if needed
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                if (tab.text == "Star") {
                    showReviewFilterBottomSheet()
                }
            }
        })
    }

    private fun showReviewFilterBottomSheet() {
        val bottomSheetFragment = ReviewFilterBottomSheetFragment()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    override fun onRatingSelected(ratings: Set<Float>) {
        // Use the ViewModel to apply the filter
        viewModel.applyFilter(ratings)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}