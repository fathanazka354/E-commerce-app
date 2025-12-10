package com.fathan.e_commerce.features.chat.domain.entity

import android.net.Uri
import androidx.compose.ui.graphics.Color

enum class MessageType { TEXT, IMAGE, AUDIO, REPLY }

data class ChatMessage(
    // IMPORTANT: use server id (UUID) as String now
    val id: String,
    val roomId: String,
    val name: String? = null,
    val senderId: String? = null,
    val content: String? = null,
    val messageType: String? = null,
    val mediaUrl: String? = null,
    val metadata: Map<String, String>? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null,
    val senderName: String? = null,
    val message: String? = null,     // text content / caption
    val isMe: Boolean = false,
    val initial: String = "?",
    val avatarColor: Color = Color(0xFFE1BEE7),
    val isOnline: Boolean = false,
    val isTyping: Boolean = false,
    val time: String = "",           // shown time like "09:12"
    val unreadCount: Int = 0,
    val imageUri: Uri? = null,
    val audioUri: Uri? = null,
    val audioDuration: String? = null,
    val type: MessageType = MessageType.TEXT,
    val replyToName: String? = null,
    val replyText: String? = null,
    val reactions: List<String> = emptyList(),
    val date: String = ""            // yyyy-mm-dd for grouping
)