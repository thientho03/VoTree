package com.example.votree.users.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.models.Friend
import com.example.votree.users.adapters.FriendAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var friendAdapter: FriendAdapter
    private val friendsList = mutableListOf<Friend>()
    private lateinit var backButton: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.recycles_view_chat_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        friendAdapter = FriendAdapter(friendsList) { friend ->
            openChat(friend)
        }
        recyclerView.adapter = friendAdapter

        backButton = findViewById(R.id.btnChatBack)
        backButton.setOnClickListener {
            finish()
        }

        loadFriends()
    }

    private fun openChat(friend: Friend) {
        val intent = Intent(this, MessageActivity::class.java).apply {
            putExtra("FRIEND_ID", friend.uid)
            putExtra("FRIEND_NAME", friend.name)
        }
        startActivity(intent)
    }

    private fun loadFriends() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val realtimeDatabaseRef =
            FirebaseDatabase.getInstance().getReference("Users/${currentUser?.uid}/friends")
        val firestore = FirebaseFirestore.getInstance()

        realtimeDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                friendsList.clear()
                for (snapshot in dataSnapshot.children) {
                    val friendUid = snapshot.value.toString()

                    firestore.collection("users").document(friendUid).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val friendName = document.getString("username") ?: "Unnamed"
                                val friendAvatar = document.getString("avatar") ?: ""

                                FirebaseDatabase.getInstance()
                                    .getReference("conversations/${currentUser?.uid}-$friendUid")
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(conversationSnapshot: DataSnapshot) {
                                            val lastMessage =
                                                conversationSnapshot.child("lastMessage")
                                                    .getValue(String::class.java) ?: "No message"
                                            val lastMessageTime =
                                                conversationSnapshot.child("timestamp")
                                                    .getValue(Long::class.java) ?: 0L

                                            val friend = Friend(
                                                uid = friendUid,
                                                name = friendName,
                                                lastMessage = lastMessage,
                                                lastMessageTime = lastMessageTime,
                                                avatar = friendAvatar
                                            )
                                            friendsList.find { it.uid == friendUid }?.let {
                                                it.lastMessage = lastMessage
                                                it.lastMessageTime = lastMessageTime
                                            } ?: friendsList.add(friend)

                                            friendsList.sortByDescending { it.lastMessageTime }
                                            friendAdapter.notifyDataSetChanged()
                                        }

                                        override fun onCancelled(conversationError: DatabaseError) {
                                            Log.e(
                                                "ChatActivity",
                                                "Failed to read conversation data",
                                                conversationError.toException()
                                            )
                                        }
                                    })

                            } else {
                                Log.d("ChatActivity", "No such document")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("ChatActivity", "get failed with ", exception)
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ChatActivity", "Failed to read friends data", databaseError.toException())
            }
        })
    }

}
