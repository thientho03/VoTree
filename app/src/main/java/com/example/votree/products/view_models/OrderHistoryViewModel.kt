package com.example.votree.products.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.votree.products.repositories.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers

class OrderHistoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val transactionRepository = TransactionRepository(db)

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val transactions = transactionRepository.getTransactions(userId)
        .asLiveData(Dispatchers.IO)
}
