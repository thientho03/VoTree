package com.example.votree.products.repositories

import android.util.Log
import com.example.votree.products.models.Transaction
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TransactionRepository(private val db: FirebaseFirestore) {
    suspend fun createAndUpdateTransaction(transaction: Transaction): String {
        val generatedId = createTransaction(transaction)
        updateTransactionId(transaction, generatedId)
        updateUserTransactionIdList(transaction.customerId, generatedId)
        updateStoreTransactionIdList(transaction.storeId, generatedId)
        return generatedId
    }

    private suspend fun createTransaction(transaction: Transaction): String {
        return withContext(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val transactionsCollection = db.collection("transactions")

            val documentReference = transactionsCollection.add(transaction).await()
            documentReference.id
        }
    }

    private suspend fun updateTransactionId(transaction: Transaction, generatedId: String) {
        withContext(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val transactionsCollection = db.collection("transactions")

            transactionsCollection.document(generatedId).update("id", generatedId).await()
        }
    }

    private suspend fun updateUserTransactionIdList(customerId: String, transactionId: String) {
        withContext(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val usersCollection = db.collection("users")

            usersCollection.document(customerId)
                .update("transactionIdList", FieldValue.arrayUnion(transactionId))
                .await()
        }
    }

    private suspend fun updateStoreTransactionIdList(storeId: String, transactionId: String) {
        withContext(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val storesCollection = db.collection("stores")

            storesCollection.document(storeId)
                .update("transactionIdList", FieldValue.arrayUnion(transactionId))
                .await()
        }
    }

    suspend fun fetchShopName(storeId: String): String? {
        val db = FirebaseFirestore.getInstance()
        Log.d("TransactionRepository", "Fetching shop name for storeId: $storeId")
        val storeDoc = db.collection("stores").document(storeId).get().await()
        return storeDoc.getString("storeName")
    }

    fun calculateTotalQuantity(productsMap: MutableMap<String, Int>): Int {
        return productsMap.values.sum()
    }

    suspend fun calculateTotalPrice(productsMap: MutableMap<String, Int>): Double {
        return productsMap.entries.sumOf { (productId, quantity) ->
            val productDoc = db.collection("products").document(productId).get().await()
            val price = productDoc.getDouble("price") ?: 0.0
            price * quantity
        }
    }

    // Function to fetch transactions for a specific user
    fun getTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val transactionsCollection = db.collection("transactions")
        val query = transactionsCollection.whereEqualTo("customerId", userId)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) } ?: emptyList()
            trySend(transactions).isSuccess
        }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun isReviewSubmitted(transactionId: String, userId: String): Boolean {
        val db = FirebaseFirestore.getInstance()
        val productReviewsCollection = db.collection("productReviews")
        val query = productReviewsCollection.whereEqualTo("transactionId", transactionId)
            .whereEqualTo("userId", userId)

        val querySnapshot = query.get().await()
        return !querySnapshot.isEmpty
    }

    suspend fun fetchOrdersForStore(storeId: String): List<Transaction> {
        return withContext(Dispatchers.IO) {
            val transactionList = mutableListOf<Transaction>()
            val transactionRef = FirebaseFirestore.getInstance().collection("transactions")
                .whereEqualTo("storeId", storeId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            for (document in transactionRef.documents) {
                val transaction = document.toObject(Transaction::class.java)
                transaction?.let { transactionList.add(it) }
            }

            transactionList
        }
    }

    suspend fun getTransaction(transactionId: String): Transaction {
        return withContext(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val transactionsCollection = db.collection("transactions")

            val transactionDoc = transactionsCollection.document(transactionId).get().await()
            transactionDoc.toObject(Transaction::class.java) ?: Transaction()
        }
    }

    suspend fun toggleOrderStatus(transactionId: String, status: String) {
        withContext(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val transactionsCollection = db.collection("transactions")

            transactionsCollection.document(transactionId).update("status", status).await()
        }
    }

    suspend fun getTransactionDate(storeId: String): Timestamp {
        val transactionsCollection = db.collection("transactions")

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        val dateString = "31/12/${currentYear - 1}"
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val lastDayOfPreviousYear = dateFormat.parse(dateString)

        val lastDayTimestamp = Timestamp(lastDayOfPreviousYear)

        val firstDateDoc = transactionsCollection
            .whereEqualTo("storeId", storeId)
            .whereGreaterThan("createdAt", lastDayTimestamp)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        val firstDate = firstDateDoc?.getTimestamp("date") ?: Timestamp(Date(0))

        return firstDate
    }
}