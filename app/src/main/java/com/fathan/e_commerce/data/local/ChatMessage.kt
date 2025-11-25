package com.fathan.e_commerce.data.local

import android.net.Uri
import androidx.compose.ui.graphics.Color
import java.time.LocalDate

enum class MessageType {
    TEXT,
    REPLY,
    IMAGE,
    AUDIO
}

data class Message(
    val id: Int,
    val text: String,
    val senderName: String,
    val time: String,
    val date: LocalDate,
    val isMe: Boolean,
    val initial: String,
    val avatarColor: Color,
    val type: MessageType = MessageType.TEXT,
    val imageUri: Uri? = null,
    val audioUri: Uri? = null,
    val audioDuration: String? = null, // e.g. "0:15"
    val replyToName: String? = null,
    val replyText: String? = null,
    val reactions: List<String> = emptyList()
)
