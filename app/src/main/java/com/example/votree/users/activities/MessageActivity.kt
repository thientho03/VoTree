package com.example.votree.users.activities

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.models.Message
import com.example.votree.users.adapters.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MessageActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()
    private lateinit var inputText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_message)

        val friendName = intent.getStringExtra("FRIEND_NAME") ?: "Unknown"
        val friendId = intent.getStringExtra("FRIEND_ID")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        findViewById<TextView>(R.id.message_friend_username).text = friendName

        recyclerView = findViewById(R.id.recycles_view_chat_box)
        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messageList, currentUserId)
        recyclerView.adapter = messageAdapter

        inputText = findViewById(R.id.message_input_text)
        sendButton = findViewById(R.id.send_message_button)
        sendButton.setOnClickListener {
            val messageText = inputText.text.toString()
            if (messageText.isNotEmpty()) {
                if (friendId != null) {
                    sendMessage(currentUserId, friendId, messageText)
                }
                inputText.text.clear()
            }
        }

        backButton = findViewById(R.id.btnMessageBack)
        backButton.setOnClickListener {
            finish()
        }

        val chatId = if (currentUserId > friendId.toString()) "$currentUserId-$friendId" else "$friendId-$currentUserId"
        loadMessages(chatId)
    }

    private fun sendMessage(senderId: String, receiverId: String, messageText: String) {
        val chatId = if (senderId > receiverId) "$senderId-$receiverId" else "$receiverId-$senderId"
        val message = Message(senderId, receiverId, messageText, System.currentTimeMillis())
        val database = FirebaseDatabase.getInstance()
        val messageId = database.getReference("Messages/$chatId").push().key ?: return

        database.getReference("Messages/$chatId/$messageId").setValue(message).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val lastMessageInfo = mapOf("lastMessageId" to messageId, "lastMessage" to messageText, "timestamp" to System.currentTimeMillis())
                database.getReference("conversations/$chatId").updateChildren(lastMessageInfo)
            }
        }
    }


    private fun loadMessages(chatId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Messages/$chatId")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messageList.clear()
                dataSnapshot.children.mapNotNullTo(messageList) { it.getValue(Message::class.java) }
                messageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MessageActivity", "Failed to read messages", databaseError.toException())
            }
        })
    }
}