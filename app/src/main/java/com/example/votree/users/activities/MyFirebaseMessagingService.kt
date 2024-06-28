package com.example.votree.users.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.votree.MainActivity
import com.example.votree.R
import com.example.votree.notifications.models.Notification
import com.example.votree.notifications.view_models.NotificationViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        println("From: ${remoteMessage.from}")

        // Check for notification payload
        remoteMessage.notification?.let {
            println("Message Notification Body: ${it.body}")
            sendNotification(remoteMessage.from ?: "", it.body ?: "")
        }

        // Extract orderId from the data payload
        val orderId = remoteMessage.data["orderId"] ?: ""

        val notification = Notification(
            title = remoteMessage.notification?.title ?: "No Title",
            content = remoteMessage.notification?.body ?: "No Content",
            orderId = orderId
        )

        saveNotification(notification)
    }

    private fun saveNotification(notification: Notification) {
        val notificationViewModel = NotificationViewModel()
        notificationViewModel.saveNotification(notification)
    }

    private fun sendNotification(from: String, body: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this@MyFirebaseMessagingService, "$body", Toast.LENGTH_SHORT)
                .show()
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "My channel ID"
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_info)
            .setContentTitle("VoTree")
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val userId = Firebase.auth.currentUser?.uid ?: ""
        if (userId.isEmpty()) {
            Log.w(TAG, "User ID is empty")
            return
        }

        // Update the token for the user
        updateToken("users", userId, token)

        // Check if the user has a storeId and update the token for the store
        Firebase.firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val storeId = document.getString("storeId")
                if (!storeId.isNullOrEmpty()) {
                    updateToken("stores", storeId, token)
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to fetch user profile for storeId", e)
            }
    }

    private fun updateToken(collectionPath: String, id: String, token: String) {
        val deviceToken = hashMapOf(
            "token" to token,
            "timestamp" to FieldValue.serverTimestamp()
        )

        Firebase.firestore.collection("fcmTokens").document(collectionPath).collection(id)
            .document("deviceToken")
            .set(deviceToken)
            .addOnSuccessListener {
                Log.d(TAG, "Device token updated for $collectionPath/$id")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating device token for $collectionPath/$id", e)
            }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
