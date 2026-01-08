package com.fathan.e_commerce.data.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fathan.e_commerce.data.repository.FCMTokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMManager @Inject constructor(
    private val fcmTokenRepository: FCMTokenRepository
) {
    companion object {
        private const val TAG = "FCMManager"
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No permission needed for Android < 13
        }
    }

    /**
     * Request notification permission
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    /**
     * Initialize FCM - Get token and save to Supabase
     */
    fun initializeFCM(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Initializing FCM...")

                fcmTokenRepository.getFCMToken()
                    .onSuccess { token ->
                        Log.d(TAG, "FCM Token retrieved: $token")

                        // Save to Supabase
                        fcmTokenRepository.saveFCMToken(token)
                            .onSuccess {
                                Log.d(TAG, "FCM token saved to Supabase")
                            }
                            .onFailure { error ->
                                Log.e(TAG, "Failed to save FCM token to Supabase", error)
                            }
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Failed to get FCM token", error)
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Error initializing FCM", e)
            }
        }
    }

    /**
     * Clear FCM token on logout
     */
    fun clearFCMToken(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Clearing FCM token...")

                fcmTokenRepository.deleteFCMToken()
                    .onSuccess {
                        Log.d(TAG, "FCM token cleared successfully")
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Failed to clear FCM token", error)
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Error clearing FCM token", e)
            }
        }
    }
}