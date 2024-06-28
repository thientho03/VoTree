package com.example.votree.users.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.FragmentStoreProfile2Binding
import com.example.votree.products.adapters.ProductAdapter
import com.example.votree.products.view_models.ProductViewModel
import com.example.votree.tips.AdManager
import com.example.votree.users.view_models.ProfileViewModel
import com.example.votree.utils.GridSpacingItemDecoration
import java.text.SimpleDateFormat
import com.example.votree.utils.uiUtils.Companion.calculateNoOfColumns
import com.google.android.gms.ads.AdView
import java.util.Locale

class StoreProfile2 : Fragment() {
    private lateinit var binding: FragmentStoreProfile2Binding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private val args: StoreProfile2Args by navArgs()
    private val pageSize = 5
    private var isLoading = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoreProfile2Binding.inflate(inflater, container, false)
        setupProfileData()
        setupProductData()
        setupToolbar()

        val adView = binding.root.findViewById<AdView>(R.id.adView)
        AdManager.addAdView(adView, requireActivity())
        return binding.root
    }

    private fun setupProfileData() {
        val storeId = args.storeId
        Log.d("StoreProfile2", "Store ID: $storeId")
        binding.storeProfile2Toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        val formatter = SimpleDateFormat("dd/mm/yyyy", Locale.getDefault())

        // Query user from database
        profileViewModel.queryStore(storeId).observe(viewLifecycleOwner) { userStore ->
            binding.storeProfile2Name.text = userStore.store.storeName
            Glide.with(requireActivity())
                .load(userStore.user.avatar)
                .placeholder(R.drawable.img_placeholder)
                .into(binding.storeProfile2Avatar)
            addProfileField(binding, "Username", userStore.user.username)
            addProfileField(binding, "Email", userStore.user.email)
            addProfileField(binding, "Phone Number", userStore.user.phoneNumber)
            addProfileField(binding, "Store Name", userStore.store.storeName)
            addProfileField(binding, "Store Location", userStore.store.storeLocation)
            addProfileField(binding, "Join Date", formatter.format(userStore.user.createdAt))
        }
    }

    private fun addProfileField(
        binding: FragmentStoreProfile2Binding,
        label: String,
        value: String
    ) {
        fun ProfileField(label: String, value: String): LinearLayout {
            val baseLayout = layoutInflater.inflate(
                R.layout.profile_information_item,
                binding.storeProfile2InfoContainer,
                false
            )
            baseLayout.findViewById<TextView>(R.id.store_profile_2_field_label).text = label
            baseLayout.findViewById<TextView>(R.id.store_profile_2_field_value).text = value
            return baseLayout as LinearLayout
        }
        Log.d("StoreProfile2", "Adding profile field: $label - $value")
        binding.storeProfile2InfoContainer.addView(ProfileField(label, value))
    }

    private fun setupToolbar() {
        binding.storeProfile2Toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.storeProfile2Toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.storeProfile2_to_StoreReport -> {
                    val action =
                        StoreProfile2Directions.actionStoreProfile2ToStoreReport(args.storeId)
                    findNavController().navigate(action)
                    true
                }

                else -> false
            }
        }
    }

    private fun setupProductData() {
        val numberOfColumns = calculateNoOfColumns(requireContext())
        binding.storeProfile2ProductsList.apply {
            adapter = ProductAdapter()
            layoutManager = GridLayoutManager(requireContext(), numberOfColumns)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) {
                        super.onScrolled(recyclerView, dx, dy)

                        val layoutManager = recyclerView.layoutManager as GridLayoutManager
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                        if (visibleItemCount + firstVisibleItemPosition + 4 >= totalItemCount && firstVisibleItemPosition >= 0) {
                            loadMoreData()
                        }
                    }
                }
            })
        }

        binding.storeProfile2ProductsList.addItemDecoration(
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
            productViewModel.lastVisibleProduct.value?.let { lastVisible ->
                productViewModel.fetchProductsPerPage(lastVisible, pageSize)
            }
        }
    }
}