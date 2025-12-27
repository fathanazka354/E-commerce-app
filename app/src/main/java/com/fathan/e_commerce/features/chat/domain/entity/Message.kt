package com.fathan.e_commerce.features.chat.domain.entity

import android.net.Uri
import androidx.compose.ui.graphics.Color


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

data class ChatMessage(
    val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String? = null,
    val otherUserName: String? = null,
    val otherUserEmail: String? = null,
    val otherUserAvatar: String? = null,

    val message: String? = null,
    val type: MessageType = MessageType.TEXT,

    val mediaUrl: String? = null,
    val metadata: Map<String, String>? = null,

    val isRead: Boolean = false,
    val isMe: Boolean = true,

    val createdAt: String,
    val time: String = "",
    val date: String = "",

    // UI helpers
    val initial: String = "?",
    val avatarColor: Color = Color(0xFFE1BEE7),
    val imageUri: Uri? = null,
    val audioUri: Uri? = null,
    val audioDuration: String? = null
)
