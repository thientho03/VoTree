package com.example.votree.products.repositories

import com.example.votree.products.models.PointTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class PointTransactionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val pointTransactionCollection = db.collection("users/${userId}/PointTransactions")

    suspend fun addPointTransaction(transaction: PointTransaction) {
        try {
            val documentReference = pointTransactionCollection.add(transaction).await()
            pointTransactionCollection.document(documentReference.id)
                .update("id", documentReference.id).await()
            println("DocumentSnapshot added with ID: ${documentReference.id}")
        } catch (e: Exception) {
            println("Error adding document: $e")
        }
    }

    suspend fun getCurrentPoints(): Int {
        val result = pointTransactionCollection.get().await()
        val earnedPoints = result.documents.filter { it["type"] == "earn" }
            .sumOf { (it["points"] as Long).toInt() }
        val redeemedPoints = result.documents.filter { it["type"] == "redeem" }
            .sumOf { (it["points"] as Long).toInt() }
        return earnedPoints - redeemedPoints
    }

    suspend fun getPointTransactionsForUser(): List<PointTransaction> {
        val result =
            pointTransactionCollection.orderBy("transactionDate", Query.Direction.DESCENDING).get()
                .await()
        return result.documents.map { it.toObject(PointTransaction::class.java)!! }
    }

    suspend fun redeemPoints(pointsToRedeem: Int, description: String): Boolean {
        val currentPoints = getCurrentPoints()
        if (currentPoints >= pointsToRedeem) {
            val redemptionTransaction = PointTransaction(
                userId = userId,
                points = pointsToRedeem,
                type = "redeem",
                description = description,
                transactionDate = Date()
            )
            addPointTransaction(redemptionTransaction)
            return true
        } else {
            throw Exception("Not enough points to redeem")
        }
    }
}
