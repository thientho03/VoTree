package com.example.votree.products.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votree.products.models.ShippingAddress
import com.example.votree.products.repositories.ShippingAddressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ShippingAddressViewModel : ViewModel() {
    private val repository =
        ShippingAddressRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())

    private val _shippingAddress = MutableLiveData<ShippingAddress?>()
    val shippingAddress: LiveData<ShippingAddress?> = _shippingAddress

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchShippingAddress()
    }

    fun fetchShippingAddress() {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                val address = repository.getDefaultShippingAddress()
                _shippingAddress.postValue(address)
            } catch (e: Exception) {
                Log.e("ShippingAddressViewModel", "Error fetching shipping address: $e")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun saveShippingAddress(address: ShippingAddress) {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                repository.saveShippingAddress(address, userId)
                _shippingAddress.postValue(address)
            } catch (e: Exception) {
                Log.e("ShippingAddressViewModel", "Error saving shipping address: $e")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun updateShippingAddress(address: ShippingAddress) {
        _shippingAddress.postValue(address)
    }
}