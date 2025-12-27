package com.fathan.e_commerce.features.chat.domain.entity

import android.net.Uri
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName


enum class MessageType {
    TEXT, IMAGE, AUDIO, REPLY;

    companion object {
        fun from(raw: String?): MessageType =
            when (raw?.lowercase()) {
                "image" -> IMAGE
                "audio" -> AUDIO
                "reply" -> REPLY
                else -> TEXT
            }
    }
}

// Message.kt
data class Message(
    val id: String,

    val conversationId: String,

    val senderId: String,

    val messageType: String, // "text", "image", "product_card", "system"

    val messageContent: String,

    val productId: Long?,

    val isRead: Boolean,

    val readAt: String?,

    val createdAt: String,

    val updatedAt: String
)