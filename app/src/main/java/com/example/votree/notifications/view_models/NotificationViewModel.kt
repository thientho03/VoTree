package com.example.votree.notifications.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votree.notifications.models.Notification
import com.example.votree.products.models.Product
import com.example.votree.products.models.Transaction
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.products.repositories.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    private val _transactionData = MutableLiveData<Transaction?>()
    val transactionData: LiveData<Transaction?> = _transactionData

    private val _productData = MutableLiveData<Product?>()
    val productData: LiveData<Product?> = _productData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val loadedProducts = mutableMapOf<String, Product>()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        Log.d("NotificationViewModel", "Fetching notifications")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("users/$userId/notifications")
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                val notificationsList = snapshot.toObjects(Notification::class.java)
                _notifications.postValue(notificationsList)
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error fetching notifications", e)
            }
            _isLoading.value = false
        }
    }

    fun saveNotification(notification: Notification) {
        viewModelScope.launch {
            val notificationMap = hashMapOf(
                "title" to notification.title,
                "content" to notification.content,
                "read" to notification.read,
                "createdAt" to com.google.firebase.Timestamp(notification.createdAt),
                "orderId" to notification.orderId  // Include orderId in the map
            )

            try {
                val documentReference = db.collection("users/$userId/notifications").add(notificationMap).await()
                val notificationId = documentReference.id
                db.collection("users/$userId/notifications").document(notificationId)
                    .update("id", notificationId).await()
                _notifications.value?.let {
                    _notifications.postValue(it.plus(notification))
                }
            } catch (e: Exception) {
                Log.d("NotificationViewModel", "Error saving notification", e)
                // Handle exceptions
            }
        }
    }

    fun removeReadNotifications() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users/$userId/notifications")
                    .whereEqualTo("isRead", true)
                    .get()
                    .await()
                for (document in snapshot.documents) {
                    db.collection("users/$userId/notifications").document(document.id).delete().await()
                }
            } catch (e: Exception) {
                Log.d("NotificationViewModel", "Error removing read notifications", e)
                // Handle exceptions
            }
        }
    }

    fun updateNotificationReadStatus(notificationId: String, isRead: Boolean) {
        viewModelScope.launch {
            try {
                db.collection("users/$userId/notifications").document(notificationId)
                    .update("read", isRead).await()
            } catch (e: Exception) {
                Log.d("NotificationViewModel", "Error updating notification read status", e)
            }
        }
    }

    fun fetchTransactionInfo(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val transactionRepository = TransactionRepository(FirebaseFirestore.getInstance())
                val transaction = transactionRepository.getTransaction(orderId)
                _transactionData.postValue(transaction)
            } catch (e: Exception) {
                // Handle the error
            }
        }
    }

    fun fetchProductInfo(notification: Notification) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val transactionRepository = TransactionRepository(FirebaseFirestore.getInstance())
                val transaction = transactionRepository.getTransaction(notification.orderId)
                val productRepository = ProductRepository(FirebaseFirestore.getInstance())

                val firstProductId = transaction.productsMap.keys.firstOrNull()
                if (firstProductId != null) {
                    if (loadedProducts.containsKey(firstProductId)) {
                        _productData.postValue(loadedProducts[firstProductId])
                    } else {
                        val product = productRepository.getProduct(firstProductId)
                        loadedProducts[firstProductId] = product
                        _productData.postValue(product)
                    }
                }
            } catch (e: Exception) {
                // Handle the error
                Log.e("NotificationViewModel", "Error fetching product info", e)
            }
        }
    }

}