package com.example.votree.products.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votree.products.models.Product
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")

    private var searchJob: Job? = null
    private val suggestionCache = mutableMapOf<String, List<String>>()

    private val _products: MutableLiveData<List<Product>> = MutableLiveData()
    val products: LiveData<List<Product>>
        get() = _products

    init {
//        fetchProducts()
    }

     fun fetchProducts() {
        productsCollection
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    Log.e(TAG, "Error fetching products: $error")
                    return@addSnapshotListener
                }

                val productsList = mutableListOf<Product>()
                snapshot?.let {
                    for (doc in it.documents) {
                        val product = doc.toObject(Product::class.java)
                        product?.let { product ->
                            productsList.add(product)
                        }
                    }
                }
                _products.value =
                    productsList  // Assuming _products is a MutableLiveData<List<Product>>
            }
    }

    fun getProductById(productId: String): LiveData<Product?> {
        val productLiveData = MutableLiveData<Product?>()

        productsCollection.document(productId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
            Log.d(TAG, "Fetched product with ID: $documentSnapshot")
            val product = documentSnapshot.toObject(Product::class.java)
            productLiveData.value = product
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error fetching product with ID: $productId", e)
            productLiveData.value = null
        }

        return productLiveData
    }

    // Function to get product price by id
    fun getProductPriceById(productId: String): Double? {
        val product = _products.value?.find { it.id == productId }
        Log.d("_product", _products.value.toString())
        return product?.price
    }

    private fun filterProductNames(products: List<Product>, query: String): List<Product> {
        val queryLowercase = query.lowercase()  // Convert the query to lowercase for case-insensitive comparison

        return products.filter { product ->
            // Check if the product name contains the query string at any position, case-insensitively
            product.productName.lowercase().contains(queryLowercase)
        }
    }


    private fun fetchProductSuggestions(query: String): MutableLiveData<List<Product>> {
        val filteredProductsLiveData = MutableLiveData<List<Product>>()

        productsCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.documents.mapNotNull { document ->
                    document.toObject(Product::class.java)
                }
                val filteredProducts = filterProductNames(products, query)
                filteredProductsLiveData.value = filteredProducts
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error fetching filtered product: $error")
                filteredProductsLiveData.value = emptyList()
            }

        return filteredProductsLiveData
    }

    fun searchProducts(query: String): LiveData<List<Product>> {
        val filteredProductsLiveData = MutableLiveData<List<Product>>()

        productsCollection
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.toObjects(Product::class.java)
                val filteredProduct = filterProductNames(products, query)
                filteredProductsLiveData.value = filteredProduct
                Log.d(TAG, "Fetched ${filteredProduct.size} products")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error fetching filtered product: $error")
                filteredProductsLiveData.value = emptyList()
            }
        return filteredProductsLiveData
    }

    fun getDebouncedProductSuggestions(query: String): LiveData<List<String>> {
        searchJob?.cancel()
        val result = MutableLiveData<List<String>>()
        searchJob = viewModelScope.launch {
            delay(300)
            fetchProductSuggestions(query).observeForever {
                result.value = it.map { product -> product.productName }
            }
        }
        return result
    }

    fun getCachedSuggestions(query: String): LiveData<List<String>> {
        val liveData = MutableLiveData<List<String>>()
        if (suggestionCache.containsKey(query)) {
            liveData.value = suggestionCache[query]
        } else {
            liveData.value = emptyList()
            fetchProductSuggestions(query).observeForever { suggestions ->
                suggestionCache[query] = suggestions.map { product -> product.productName }
                liveData.value = suggestionCache[query]
            }
        }
        return liveData
    }

    fun sortProductsByPrice(ascending: Boolean = true) {
        if (ascending) {
            _products.value = _products.value?.sortedBy { it.price }
        } else {
            _products.value = _products.value?.sortedByDescending { it.price }
        }
    }

    fun sortProductsBySoldQuantity() {
        _products.value = _products.value?.sortedByDescending { it.quantitySold }
    }

    fun sortProductsByCreationDate() {
        _products.value = _products.value?.sortedByDescending { it.createdAt }
    }

    private val _lastVisibleProduct: MutableLiveData<DocumentSnapshot?> = MutableLiveData()
    val lastVisibleProduct: LiveData<DocumentSnapshot?> = _lastVisibleProduct

    fun fetchProductsPerPage(lastVisibleProduct: DocumentSnapshot?, pageSize: Int) {
        var query = productsCollection
            .whereEqualTo("active", true)
            .orderBy("quantitySold", com.google.firebase.firestore.Query.Direction.DESCENDING)

        lastVisibleProduct?.let {
            query = query.startAfter(it)
            Log.d(TAG, "Fetching next page of products after: ${it.id}")
        }
        query = query.limit(pageSize.toLong())
        Log.d(TAG, "Fetch ${pageSize.toLong()} products")

        query.get()
            .addOnSuccessListener { snapshot ->
                val newProducts = snapshot.toObjects(Product::class.java)
                Log.d(TAG, "Fetched ${newProducts.size} products")

                newProducts.forEach { product ->
                    Log.d(TAG, "Product Fetch: ${product.id} - ${product.productName}")
                }

                val currentProducts = _products.value ?: listOf()

                val uniqueNewProducts = newProducts.filter { newProduct ->
                    currentProducts.none { it.id == newProduct.id }
                }

                _products.value = currentProducts + uniqueNewProducts
                Log.d(TAG, "Total products: ${_products.value?.size}")

                if (!snapshot.isEmpty) {
                    Log.d(TAG, "Setting last visible product")
                    _lastVisibleProduct.value = snapshot.documents[snapshot.size() - 1]
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error fetching products: $error")
            }
    }

    suspend fun getProduct(productId: String): Product? {
        return productsCollection.document(productId)
            .get()
            .await()
            .toObject(Product::class.java)
    }

    companion object {
        private const val TAG = "ProductViewModel"
    }
}

