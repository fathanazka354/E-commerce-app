package com.fathan.e_commerce.data.utils

import android.content.Intent
import android.net.Uri

fun extractTokenFromIntent(intent: Intent?): String? {
    val data = intent?.data ?: return null

    // 1) try query params first
    val qAccessToken = data.getQueryParameter("access_token")
    val qToken = data.getQueryParameter("token")
    if (!qAccessToken.isNullOrBlank()) return qAccessToken
    if (!qToken.isNullOrBlank()) return qToken

    // 2) try fragment (#access_token=...)
    val fragment = data.fragment // returns string after '#', or null
    if (!fragment.isNullOrBlank()) {
        val params = fragment.split("&")
            .mapNotNull {
                val parts = it.split("=")
                if (parts.size >= 2) parts[0] to parts.drop(1).joinToString("=") else null
            }.toMap()
        val fAccess = params["access_token"] ?: params["token"]
        if (!fAccess.isNullOrBlank()) return fAccess
    }

    // 3) fallback: check whole data string for "access_token=" pattern
    val dataStr = data.toString()
    val fallbackRegex = Regex("""(?:access_token|token)=([^&]+)""")
    val match = fallbackRegex.find(dataStr)
    if (match != null) return Uri.decode(match.groupValues[1])

    return null
}
