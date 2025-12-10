package com.fathan.e_commerce

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Utils {

    // --- Date Helper ---
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayHeader(date: LocalDate): String {
        val today = LocalDate.now()
        return when {
            date.isEqual(today) -> "TODAY"
            date.isEqual(today.minusDays(1)) -> "YESTERDAY"
            else -> date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTimeAgo(isoString: String): String {
        val zoneId = ZoneId.systemDefault()

        val time = LocalDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME)
        val messageTime = time.atZone(zoneId)
        val now = ZonedDateTime.now(zoneId)

        val diff = Duration.between(messageTime, now)

        val minutes = diff.toMinutes()
        val hours = diff.toHours()
        val days = diff.toDays()

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes minutes ago"
            hours == 1L -> "1 hour ago"
            hours < 24 -> "$hours hours ago"
            days == 1L -> "Yesterday"
            days in 2..6 -> "$days days ago"
            days in 7..13 -> "A week ago"
            days in 14..29 -> "${days / 7} weeks ago"
            days in 30..59 -> "1 month ago"
            days < 365 -> "${days / 30} months ago"
            days in 365..729 -> "1 year ago"
            else -> "${days / 365} years ago"
        }
    }

}