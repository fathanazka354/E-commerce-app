package com.fathan.e_commerce.data.notification

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMNotificationService @Inject constructor() {

    companion object {
        private const val TAG = "FCMNotificationService"
        private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"

        // ⚠️ WARNING: For portfolio/demo only!
        // In production, NEVER store server key in app!
        private const val FCM_SERVER_KEY = "your-fcm-server-key-here"
    }

    private val client = OkHttpClient()

    suspend fun sendNotification(
        recipientToken: String,
        title: String,
        body: String,
        conversationId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("to", recipientToken)
                put("priority", "high")
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", body)
                    put("sound", "default")
                })
                put("data", JSONObject().apply {
                    put("conversation_id", conversationId)
                    put("click_action", "OPEN_CHAT")
                })
            }

            val request = Request.Builder()
                .url(FCM_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "key=$FCM_SERVER_KEY")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d(TAG, "Notification sent successfully")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to send: ${response.code} ${response.message}")
                Result.failure(Exception("FCM error: ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
            Result.failure(e)
        }
    }
}