package com.fathan.e_commerce.data.repository

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface FCMTokenRepository {
    suspend fun saveFCMToken(token: String): Result<Unit>
    suspend fun getFCMToken(): Result<String>
    suspend fun deleteFCMToken(): Result<Unit>
}

@Singleton
class FCMTokenRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : FCMTokenRepository {

    companion object {
        private const val TAG = "FCMTokenRepository"
    }

    /**
     * Save FCM token to Supabase
     */
    override suspend fun saveFCMToken(token: String): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            Log.d(TAG, "Saving FCM token for user: $userId")

            // Upsert token (insert or update if exists)
            supabaseClient.postgrest["fcm_tokens"]
                .upsert(
                    mapOf(
                        "user_id" to userId,
                        "token" to token,
                        "device_type" to "android",
                        "updated_at" to "now()"
                    )
                )

            Log.d(TAG, "FCM token saved successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save FCM token", e)
            Result.failure(e)
        }
    }

    /**
     * Get current FCM token
     */
    override suspend fun getFCMToken(): Result<String> {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Retrieved FCM token: $token")
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            Result.failure(e)
        }
    }

    /**
     * Delete FCM token (e.g., on logout)
     */
    override suspend fun deleteFCMToken(): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            Log.d(TAG, "Deleting FCM token for user: $userId")

            supabaseClient.postgrest["fcm_tokens"]
                .delete {
                    filter {
                        eq("user_id", userId)
                    }
                }

            // Also delete from Firebase
            FirebaseMessaging.getInstance().deleteToken().await()

            Log.d(TAG, "FCM token deleted successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete FCM token", e)
            Result.failure(e)
        }
    }
}