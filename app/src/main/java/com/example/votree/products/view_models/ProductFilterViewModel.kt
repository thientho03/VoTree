package com.example.votree.products.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votree.products.data.productCatagories.PlantType
import com.example.votree.products.data.productCatagories.SuitClimate
import com.example.votree.products.data.productCatagories.SuitEnvironment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductFilterViewModel : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    data class FilterState(
        val availablePlantTypes: Set<PlantType> = emptySet(),
        val availableClimates: Set<SuitClimate> = emptySet(),
        val availableEnvironments: Set<SuitEnvironment> = emptySet(),
        val selectedPlantTypes: Set<PlantType> = emptySet(),
        val selectedClimates: Set<SuitClimate> = emptySet(),
        val selectedEnvironments: Set<SuitEnvironment> = emptySet(),
        val minPrice: Float = 0f,
        val maxPrice: Float = 100f
    )

    fun setAvailableFilters(plantTypes: Set<PlantType>, climates: Set<SuitClimate>, environments: Set<SuitEnvironment>) {
        viewModelScope.launch {
            _filterState.value = _filterState.value.copy(
                availablePlantTypes = plantTypes,
                availableClimates = climates,
                availableEnvironments = environments
            )
        }
    }

    fun setPriceRange(minPrice: Float, maxPrice: Float) {
        viewModelScope.launch {
            _filterState.value = _filterState.value.copy(minPrice = minPrice, maxPrice = maxPrice)
        }
    }


    fun toggleSelection(type: FilterType, value: Enum<*>) {
        viewModelScope.launch {
            val currentSelection = when (type) {
                FilterType.PLANT_TYPE -> _filterState.value.selectedPlantTypes
                FilterType.CLIMATE -> _filterState.value.selectedClimates
                FilterType.ENVIRONMENT -> _filterState.value.selectedEnvironments
            }

            val newSelection = if (currentSelection.contains(value)) {
                currentSelection - value
            } else {
                currentSelection + value
            }

            _filterState.value = when (type) {
                FilterType.PLANT_TYPE -> _filterState.value.copy(selectedPlantTypes = newSelection as Set<PlantType>)
                FilterType.CLIMATE -> _filterState.value.copy(selectedClimates = newSelection as Set<SuitClimate>)
                FilterType.ENVIRONMENT -> _filterState.value.copy(selectedEnvironments = newSelection as Set<SuitEnvironment>)
            }
        }
    }

    fun resetFilters() {
        viewModelScope.launch {
            _filterState.value = FilterState(
                availablePlantTypes = _filterState.value.availablePlantTypes,
                availableClimates = _filterState.value.availableClimates,
                availableEnvironments = _filterState.value.availableEnvironments
            )
            _currentFilterCriteria.value = FilterCriteria(
                selectedPlantTypes = emptySet(),
                selectedClimates = emptySet(),
                selectedEnvironments = emptySet(),
                minPrice = 0f,
                maxPrice = 100f
            )
        }
    }

    // Add a method to trigger the filter application process
    fun applyFilters() {
        viewModelScope.launch {
            _currentFilterCriteria.value = FilterCriteria(
                selectedPlantTypes = filterState.value.selectedPlantTypes,
                selectedClimates = filterState.value.selectedClimates,
                selectedEnvironments = filterState.value.selectedEnvironments,
                minPrice = filterState.value.minPrice,
                maxPrice = filterState.value.maxPrice
            )
        }
    }

    // LiveData to hold the current filter criteria
    private val _currentFilterCriteria = MutableLiveData<FilterCriteria>()
    val currentFilterCriteria: LiveData<FilterCriteria> = _currentFilterCriteria

    data class FilterCriteria(
        val selectedPlantTypes: Set<PlantType>,
        val selectedClimates: Set<SuitClimate>,
        val selectedEnvironments: Set<SuitEnvironment>,
        val minPrice: Float,
        val maxPrice: Float
    )

    enum class FilterType {
        PLANT_TYPE, CLIMATE, ENVIRONMENT
    }
}
