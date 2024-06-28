package com.example.votree.products.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.votree.products.models.Cart
import com.example.votree.products.models.ProductItem
import com.example.votree.products.repositories.CartRepository
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.users.repositories.StoreRepository
import com.example.votree.utils.CartItemUpdateResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CartViewModel : ViewModel() {
    private val cartRepository = CartRepository()
    private val storeRepository = StoreRepository()
    private val productViewModel = ProductViewModel()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    val groupedProducts: MutableLiveData<List<ProductItem>?> = MutableLiveData()

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _toastMessage: MutableLiveData<String> = MutableLiveData()
    var toastMessage: LiveData<String> = _toastMessage

    private val _cart: MutableLiveData<Cart?> = MutableLiveData()
    val cart: MutableLiveData<Cart?>
        get() = _cart

    init {
        fetchCart()
    }

    fun fetchCart() {
        currentUser?.uid?.let { userId ->
            cartRepository.getCart(userId).onEach { cart ->
                _cart.value = cart
            }.launchIn(viewModelScope)
        }

        _cart.observeForever { cart ->
            cart?.let {
                viewModelScope.launch {
                    groupProductsByShopId(cart)
                }
            }
        }

    }

    fun addProductToCart(productId: String, quantity: Int) {
        currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                cartRepository.addToCart(userId, productId, quantity)
            }
        }
    }

    fun updateCartItem(productId: String, quantityChange: Int) {
        currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                when (val result =
                    cartRepository.updateCartItem(userId, productId, quantityChange)) {
                    is CartItemUpdateResult.Success -> _toastMessage.postValue("Cart updated successfully")
                    is CartItemUpdateResult.InventoryExceeded -> _toastMessage.postValue("Inventory exceeded")
                    is CartItemUpdateResult.MinimumQuantityReached -> _toastMessage.postValue("Minimum quantity reached")
                    is CartItemUpdateResult.InventoryUnavailable -> _toastMessage.postValue("Inventory unavailable")
                }
            }
        }
    }

    fun removeCartItem(productId: String) {
        currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                _isLoading.postValue(true)
                cartRepository.removeCartItem(userId, productId)
                _toastMessage.postValue("Product removed from cart")
                _isLoading.postValue(false)
            }
        }
    }

    fun calculateTotalProductsPrice(cart: Cart): LiveData<Double> {
        return liveData {
            val total = cart.productsMap.entries.sumByDouble { (productId, quantity) ->
                val productRepository = ProductRepository(FirebaseFirestore.getInstance())
                val product = productRepository.getProduct(productId)
                val price = product.price
                price.times(quantity) ?: 0.0
            }
            Log.d("CartViewModel", "Total price: $total")
            emit(total)

        }
    }

    // Make sure this function is a suspending function
    private suspend fun groupProductsByShopId(cart: Cart) {
        _isLoading.postValue(true)

        val firestore = FirebaseFirestore.getInstance()
        return withContext(Dispatchers.IO) {
            if (cart.productsMap.isEmpty()) {
                groupedProducts.postValue(emptyList())
                _isLoading.postValue(false)
                return@withContext
            }

            val productIds = cart.productsMap.keys.toList()
            val productsList = productIds.mapNotNull { productId ->
                ProductRepository(firestore).getProduct(productId)
            }

            val productsGroupedByShopId = productsList.groupBy { it.storeId }
            val groupedItems = mutableListOf<ProductItem>()

            productsGroupedByShopId.forEach { (storeId, products) ->
                val store = storeRepository.fetchStoreById(storeId, {
                    groupedItems.add(ProductItem.ProductHeader(storeId, it.storeName))
                    products.forEach { product ->
                        groupedItems.add(
                            ProductItem.ProductData(
                                product,
                                cart.productsMap[product.id] ?: 0
                            )
                        )
                    }

                    _isLoading.postValue(false)
                    groupedProducts.postValue(groupedItems)
                }, {
                    Log.d("CartViewModel", "Failed to fetch store with id $storeId")
                })
            }

        }
    }
}