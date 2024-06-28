package com.example.votree.users.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.votree.users.models.Store
import com.example.votree.users.models.User
import com.example.votree.users.models.UserStore
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel(){
    private val _userStore = MutableLiveData<UserStore>()
    private val firestore = FirebaseFirestore.getInstance()

    fun queryStore(storeId: String) : LiveData<UserStore>{
        // Query user from database
        val userRef = firestore.collection("users")
        val storeRef = firestore.collection("stores")

        userRef.whereEqualTo("storeId", storeId).get()
            .addOnSuccessListener { userDocument ->
                val user = userDocument.first().toObject(User::class.java)
                storeRef.document(storeId).get()
                    .addOnSuccessListener { storeDocument ->
                        val store = storeDocument.toObject(Store::class.java)
                        if (store == null) {
                            Log.w("ProfileViewModel", "Store not found")
                            return@addOnSuccessListener
                        }
                        _userStore.value = UserStore(user, store)
                        Log.d("ProfileViewModel", "UserStore: ${_userStore.value}")
                    }
                    .addOnFailureListener { exception ->
                        Log.w("ProfileViewModel", "Error getting documents: ", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.w("ProfileViewModel", "Error getting documents: ", exception)
            }
        return _userStore
    }

    fun queryUser(uid: String) : LiveData<User?>{
        val user = MutableLiveData<User?>()
        val userRef = firestore.collection("users")

        userRef.document(uid).get().addOnSuccessListener { userDocument ->
            Log.d("ProfileViewModel", "User: ${userDocument.toObject(User::class.java)}")
            user.value = userDocument.toObject(User::class.java)
        }
        .addOnFailureListener { exception ->
            Log.w("ProfileViewModel", "Error getting documents: ", exception)
            user.value = null
        }
        return user
    }
}