package com.example.votree.users.repositories

import android.util.Log
import com.example.votree.products.repositories.ProductReviewRepository
import com.example.votree.users.models.Store
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date


class StoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storeCollection = db.collection("stores")

    suspend fun createNewStore(store: Store, userId: String) {
        val documentReference = storeCollection.add(store).await()
        val storeId = documentReference.id
        store.id = storeId

        val userDocumentReference = db.collection("users").document(userId)
        userDocumentReference.update("storeId", storeId).await()
    }

    fun fetchStoreById(
        storeId: String,
        onSuccess: (com.example.votree.users.models.Store) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        storeCollection.document(storeId).get()
            .addOnSuccessListener { document ->
                val store = document.toObject(Store::class.java)
                if (store != null) {
                    onSuccess(store)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    suspend fun fetchStore(storeId: String): Store {
        val document = storeCollection.document(storeId).get().await()
        return document.toObject(Store::class.java) ?: throw RuntimeException("Store not found")
    }

    suspend fun getNumberOfProductsOfStore(storeId: String): Int {
        try {
            val documents = db.collection("products")
                .whereEqualTo("storeId", storeId)
                .get()
                .await() // Using Kotlin coroutines to wait for the result

            return documents.size()
        } catch (e: Exception) {
            Log.e("StoreRepository", "Failed to get number of products", e)
            throw RuntimeException("Failed to get number of products", e)
        }
    }

    suspend fun getAverageProductRating(storeId: String): Float {
        try {
            val documents = db.collection("products")
                .whereEqualTo("storeId", storeId)
                .get()
                .await() // Using Kotlin coroutines to wait for the result

            var totalRating = 0f
            var totalReviews = 0

            documents.forEach { document ->
                val productId = document.id
                val productReviewRepository = ProductReviewRepository(db)

                try {
                    val rating = productReviewRepository.getProductRating(productId)
                    // If product has not review, we not calculate on its
                    if (rating != -1f) {
                        totalRating += rating
                        totalReviews++
                    }
                } catch (e: Exception) {
                    Log.d("StoreRepository", "Failed to get rating for product $productId", e)
                    throw e // Rethrow the exception to be handled by the outer catch block
                }
            }

            if (totalReviews == 0) {
                return 0f
            }

            return totalRating / totalReviews
        } catch (e: Exception) {
            Log.e("StoreRepository", "Failed to calculate average rating", e)
            throw RuntimeException("Failed to calculate average rating", e)
        }
    }

    suspend fun getStoreName(storeId: String): String {
        val document = storeCollection.document(storeId).get().await()
        return document.getString("storeName") ?: ""
    }

    suspend fun getTotalOrders(storeId: String): Float {
        val collection = db.collection("transactions")
        val query = collection
            .whereEqualTo("storeId", storeId)
        val snapshot = query.get().await()

        return snapshot.size().toFloat()
    }

    suspend fun getApproval(storeId: String): Float {
        val collection = db.collection("transactions")
        val query = collection
            .whereEqualTo("storeId", storeId)
            .whereEqualTo("status", "delivered")
        val snapshot = query.get().await()

        return snapshot.size().toFloat()
    }

    suspend fun getCancellation(storeId: String): Float {
        val collection = db.collection("transactions")
        val query = collection
            .whereEqualTo("storeId", storeId)
            .whereEqualTo("status", "denied")
        val snapshot = query.get().await()

        return snapshot.size().toFloat()
    }

    suspend fun getWeeklyTotalOrders(storeId: String, startDate: LocalDate, endDate: LocalDate): Int {
        val start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val end = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())

        val collection = db.collection("transactions")
        val query = collection
            .whereEqualTo("storeId", storeId)
            .whereGreaterThanOrEqualTo("createdAt", start)
            .whereLessThan("createdAt", end)
        val snapshot = query.get().await()

        return snapshot.size()
    }

    suspend fun getWeeklyRevenue(storeId: String, startDate: LocalDate, endDate: LocalDate): Long {
        val collection = db.collection("transactions")
        val start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val end = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())

        val query = collection
            .whereEqualTo("storeId", storeId)
            .whereEqualTo("status", "delivered")
            .whereGreaterThanOrEqualTo("createdAt", start)
            .whereLessThan("createdAt", end)

        val snapshot = query.get().await()
        var totalRevenue = 0L

        for (document in snapshot.documents) {
            val revenue = document.getLong("totalAmount") ?: 0L
            totalRevenue += revenue
        }

        return totalRevenue
    }

    suspend fun getDailyRevenueList (storeId: String, startDate: LocalDate, endDate: LocalDate) : List<BarEntry> {
        val start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val collection = db.collection("transactions")
        val dayIndices = FloatArray(7) { 0f }

        val dates = (0..6).map { startDate.plusDays(it.toLong()) }
        val dayMap = dates.mapIndexed { index, date -> date to index }.toMap()

        try {
            val query: Query = collection
                .whereEqualTo("storeId", storeId)
                .whereEqualTo("status", "delivered")
                .whereGreaterThanOrEqualTo("createdAt", start)
                .whereLessThanOrEqualTo("createdAt", end)

            val result = query.get().await()

            for (document in result.documents) {
                val timestamp = document.getTimestamp("createdAt") ?: continue
                val localDateTime = LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault())
                val date = localDateTime.toLocalDate()

                val dayIndex = dayMap[date] ?: continue

                val value = document.getDouble("totalAmount") ?: 0.0
                dayIndices[dayIndex] += value.toFloat()
            }
        } catch (e: Exception) {
            Log.e("StoreRepository", "Fail to get daily revenue: ${e.message}")
        }

        val barEntries = mutableListOf<BarEntry>()
        for (i in 0 until dayIndices.size) {
            barEntries.add(BarEntry(i.toFloat(), dayIndices[i])) // Create BarEntry with the appropriate value
        }

        return barEntries
    }
}