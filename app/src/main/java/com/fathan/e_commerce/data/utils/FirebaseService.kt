package com.fathan.e_commerce.data.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fathan.e_commerce.MainActivity
import com.fathan.e_commerce.R
import com.fathan.e_commerce.data.repository.FCMTokenRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenRepository: FCMTokenRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "chat_messages"
        private const val CHANNEL_NAME = "Chat Messages"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        serviceScope.launch {
            fcmTokenRepository.saveFCMToken(token)
                .onSuccess {
                    Log.d(TAG, "FCM token saved to Supabase")
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to save FCM token", error)
                }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "=== FCM Message Received ===")
        Log.d(TAG, "Data: ${message.data}")
        Log.d(TAG, "Notification: ${message.notification}")

        // ✅ Handle both old and new key formats
        val conversationId = message.data["conversationId"]
            ?: message.data["conversation_id"] // fallback to old format

        val messageType = message.data["messageType"]
            ?: message.data["message_type"]
            ?: "text"

        val title = message.notification?.title
            ?: message.data["senderName"]
            ?: "New Message"

        val body = message.notification?.body
            ?: when (messageType) {
                "image" -> "📷 Sent a photo"
                "audio" -> "🎤 Sent a voice message"
                else -> message.data["messageContent"] ?: "New message"
            }

        Log.d(TAG, "Showing notification - ConversationID: $conversationId")

        showNotification(
            title = title,
            body = body,
            conversationId = conversationId,
            messageType = messageType
        )
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val conversationId = data["conversation_id"]
        val messageType = data["message_type"] ?: "text"
        val senderName = data["sender_name"] ?: "Someone"

        val body = when (messageType) {
            "image" -> "📷 Sent a photo"
            "audio" -> "🎤 Sent a voice message"
            else -> data["message_content"] ?: "New message"
        }

        showNotification(
            title = senderName,
            body = body,
            conversationId = conversationId,
            messageType = messageType
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        conversationId: String?,
        messageType: String = "text"
    ) {
        // ✅ Create intent to open chat when tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            // ✅ Pass conversation ID to open specific chat
            conversationId?.let {
                putExtra("conversation_id", it)
                putExtra("open_chat", true)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            conversationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = conversationId?.hashCode() ?: System.currentTimeMillis().toInt()

        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d(TAG, "Notification shown: $title - $body")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}