package com.fathan.e_commerce.features.chat.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
object TimeUtils {

    /**
     * Convert ISO timestamp to relative time (e.g., "2 minutes ago")
     */
    fun getRelativeTime(createdAt: String?): String {
        if (createdAt == null) return "Just now"

        return try {
            val messageTime = ZonedDateTime.parse(createdAt)
            val now = ZonedDateTime.now()
            val duration = Duration.between(messageTime, now)

            when {
                duration.seconds < 60 -> "Just now"
                duration.toMinutes() < 60 -> {
                    val minutes = duration.toMinutes()
                    if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
                }
                duration.toHours() < 24 -> {
                    val hours = duration.toHours()
                    if (hours == 1L) "1 hour ago" else "$hours hours ago"
                }
                duration.toDays() < 7 -> {
                    val days = duration.toDays()
                    if (days == 1L) "Yesterday" else "$days days ago"
                }
                duration.toDays() < 30 -> {
                    val weeks = duration.toDays() / 7
                    if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
                }
                duration.toDays() < 365 -> {
                    val months = duration.toDays() / 30
                    if (months == 1L) "1 month ago" else "$months months ago"
                }
                else -> {
                    val years = duration.toDays() / 365
                    if (years == 1L) "1 year ago" else "$years years ago"
                }
            }
        } catch (e: Exception) {
            "Just now"
        }
    }

    /**
     * Get time in HH:mm format
     */
    fun getShortTime(createdAt: String?): String {
        if (createdAt == null) return ""

        return try {
            val messageTime = ZonedDateTime.parse(createdAt)
            messageTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            createdAt.takeLast(8)
        }
    }

    /**
     * Get date in readable format (e.g., "Jan 15, 2025")
     */
    fun getReadableDate(createdAt: String?): String {
        if (createdAt == null) return ""

        return try {
            val messageTime = ZonedDateTime.parse(createdAt)
            messageTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Combined time display: shows time if today, date if older
     */
    fun getSmartTime(createdAt: String?): String {
        if (createdAt == null) return "Just now"

        return try {
            val messageTime = ZonedDateTime.parse(createdAt)
            val now = ZonedDateTime.now()
            val duration = Duration.between(messageTime, now)

            when {
                duration.toHours() < 24 -> getShortTime(createdAt)
                duration.toDays() < 7 -> getRelativeTime(createdAt)
                else -> getReadableDate(createdAt)
            }
        } catch (e: Exception) {
            "Just now"
        }
    }
}