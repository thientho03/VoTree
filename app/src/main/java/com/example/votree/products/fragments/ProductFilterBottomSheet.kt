package com.example.votree.products.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.votree.databinding.ProductFilterBottomSheetBinding
import com.example.votree.products.data.productCatagories.PlantType
import com.example.votree.products.data.productCatagories.SuitClimate
import com.example.votree.products.data.productCatagories.SuitEnvironment
import com.example.votree.products.view_models.ProductFilterViewModel
import com.example.votree.products.view_models.ProductViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.RangeSlider
import kotlinx.coroutines.launch

class ProductFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: ProductFilterBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductFilterViewModel by activityViewModels()
    private val productViewModel: ProductViewModel by activityViewModels()
    private lateinit var priceRangeSlider: RangeSlider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProductFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()
        setupPriceRangeSlider()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            productViewModel.fetchProducts()
            productViewModel.products.observe(viewLifecycleOwner) { products ->
                val plantTypes = products.map { it.type }.toSet()
                val climates = products.map { it.suitClimate }.toSet()
                val environments = products.map { it.suitEnvironment }.toSet()

                viewModel.setAvailableFilters(plantTypes, climates, environments)
            }

            viewModel.filterState.collect { state ->
                Log.d("ProductFilterBottomSheet", "State: $state")
                updateChipGroups(state.availablePlantTypes, binding.plantTypeCg, ProductFilterViewModel.FilterType.PLANT_TYPE)
                updateChipGroups(state.availableClimates, binding.suitClimateCg, ProductFilterViewModel.FilterType.CLIMATE)
                updateChipGroups(state.availableEnvironments, binding.suitEnvironmentCg, ProductFilterViewModel.FilterType.ENVIRONMENT)
            }
        }
    }

    private fun setupListeners() {
        binding.btnResetAll.setOnClickListener {
            viewModel.resetFilters()
            // Reset the price range slider to the full range
            priceRangeSlider.setValues(priceRangeSlider.valueFrom, priceRangeSlider.valueTo)
            dismiss()
        }

        binding.btnApply.setOnClickListener {
            viewModel.applyFilters()
            dismiss()
        }
    }

    private fun <T : Enum<T>> updateChipGroups(values: Set<T>, chipGroup: ChipGroup, filterType: ProductFilterViewModel.FilterType) {
        chipGroup.removeAllViews()
        values.forEach { value ->
            val chip = Chip(context).apply {
                text = value.name
                isCheckable = true
                isChecked = when (filterType) {
                    ProductFilterViewModel.FilterType.PLANT_TYPE -> viewModel.filterState.value.selectedPlantTypes.contains(value as PlantType)
                    ProductFilterViewModel.FilterType.CLIMATE -> viewModel.filterState.value.selectedClimates.contains(value as SuitClimate)
                    ProductFilterViewModel.FilterType.ENVIRONMENT -> viewModel.filterState.value.selectedEnvironments.contains(value as SuitEnvironment)
                }
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.toggleSelection(filterType, value)
                } else {
                    viewModel.toggleSelection(filterType, value)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun setupPriceRangeSlider() {
        // Observe the product list to find the min and max prices
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            val minPrice = products.minByOrNull { it.price }?.price ?: 0f
            val maxPrice = products.maxByOrNull { it.price }?.price ?: 100f

            //Convert the price to Float
            val minPriceFloat = minPrice.toFloat()
            val maxPriceFloat = maxPrice.toFloat()

            // Set the slider's value range
            priceRangeSlider = binding.priceRs.apply {
                valueFrom = minPriceFloat
                valueTo = maxPriceFloat
                // Set the current slider values to the full range
                setValues(minPriceFloat, maxPriceFloat)
            }

            // Add a change listener to the slider
            priceRangeSlider.addOnChangeListener { slider, _, _ ->
                val values = slider.values
                // Update the ViewModel with the selected price range
                viewModel.setPriceRange(values[0], values[1])
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
