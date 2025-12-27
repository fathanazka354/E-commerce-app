package com.fathan.e_commerce.features.chat.domain.entity

data class Conversation (
    val roomId: String,
    val lastMessage: String?,
    val lastSenderId: String?,
    val lastMessageTime: String?,
    val messageType: MessageType,
    val otherUserName: String?,
    val isRead: Boolean,
    val otherUserAvatar: String?,
    val otherUserEmail: String?,
    val unreadCount: Int,
    val isMe: Boolean
)