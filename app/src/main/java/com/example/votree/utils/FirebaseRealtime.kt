package com.example.votree.utils

import android.util.Log
import com.example.votree.MainActivity
import com.example.votree.tips.AdManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class FirebaseRealtime {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("Users")

    private fun setupUserData(uid: String) {
        val userNode = usersRef.child(uid)

        userNode.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.d("FirebaseManager", "User data not found, creating new data")
                    userNode.child("friends").child("0").setValue("empty")
                } else {
                    Log.d("FirebaseManager", "User data found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseManager", "Failed to read user data", error.toException())
            }
        })
    }

    fun setupCurrentUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d("FirebaseManager", "Current user: $currentUser")
        currentUser?.let {
            setupUserData(it.uid)
        }
    }

    fun addFriend(currentUserId: String, friendUserId: String) {
        val currentUserFriendsRef = FirebaseDatabase.getInstance().getReference("Users/$currentUserId/friends")
        val friendFriendsRef = FirebaseDatabase.getInstance().getReference("Users/$friendUserId/friends")
        // Check if the friend is already in the current user's friends list
        currentUserFriendsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Check if the friend's user ID is already in the friends list
                for (child in snapshot.children) {
                    if (child.value == friendUserId) {
                        // Friend already added
                        Log.d("FirebaseManager", "Friend already added")
                        return@addOnSuccessListener
                    }
                }

                // Friend not added yet, add them to the current user's friends list
                updateUserFriends(currentUserFriendsRef, friendUserId)
                updateUserFriends(friendFriendsRef, currentUserId)
            } else {
                // Friends list doesn't exist yet, create it and add the friend
                currentUserFriendsRef.child("0").setValue(friendUserId)
                updateUserFriends(friendFriendsRef, currentUserId)
            }
        }.addOnFailureListener { error ->
            Log.e("FirebaseManager", "Failed to check if friend already added", error)
        }
    }

    private fun updateUserFriends(userFriendsRef: DatabaseReference, userIdToAdd: String) {
        userFriendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || snapshot.childrenCount.toInt() == 0) {
                    // Không có bạn bè, thêm ngay vào vị trí "0"
                    userFriendsRef.child("0").setValue(userIdToAdd)
                    Log.d("FirebaseManager", "Added first friend: $userIdToAdd")
                } else {
                    var emptyFound = false
                    // Duyệt qua danh sách bạn bè để tìm "empty"
                    for (child in snapshot.children) {
                        if (child.value == "empty") {
                            child.ref.setValue(userIdToAdd)
                            emptyFound = true
                            Log.d("FirebaseManager", "Replaced 'empty' with: $userIdToAdd")
                            break
                        }
                    }
                    // Nếu không tìm thấy "empty", thêm vào cuối
                    if (!emptyFound) {
                        userFriendsRef.child(snapshot.childrenCount.toString()).setValue(userIdToAdd)
                        Log.d("FirebaseManager", "Added new friend at position: ${snapshot.childrenCount}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseManager", "Failed to update friends list", error.toException())
            }
        })
    }

    fun setPremiumOnFirebase(premium: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val userNode = usersRef.child(it.uid)
            userNode.child("isPremium").setValue(premium)
        }
    }

    companion object {
        private var instance: FirebaseRealtime? = null

        fun getInstance(): FirebaseRealtime {
            if (instance == null) {
                instance = FirebaseRealtime()
            }
            return instance!!
        }
    }
}
