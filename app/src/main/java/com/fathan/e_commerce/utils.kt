package com.fathan.e_commerce

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
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
}