package com.example.votree.utils

import android.util.Log
import com.example.votree.users.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object RoleManagement {
    fun checkUserRole(firebaseAuth: FirebaseAuth, onSuccess: (String?) -> Unit) {
        // Get user from firestore and check their role
        firebaseAuth.currentUser?.uid?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.toObject(User::class.java)?.role
                    onSuccess(role)
                } else {
                    db.collection("admins").document(uid).get().addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            onSuccess("admin")
                        } else {
                            onSuccess(null)
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                Log.d("RoleManagement", "get failed with ", exception)
                onSuccess(null)
            }
        }
    }

    fun updateUserRole(firebaseAuth: FirebaseAuth, role: String) {
        // Update user role in firestore
        firebaseAuth.currentUser?.uid?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).update("role", role)
        }
    }
}