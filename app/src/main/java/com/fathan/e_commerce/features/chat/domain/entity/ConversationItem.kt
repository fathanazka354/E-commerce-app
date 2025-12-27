package com.fathan.e_commerce.features.chat.domain.entity


data class ConversationItem (
    val conversationId: String,

    val buyerId: String,

    val sellerId: String,

    val otherUserId: String,

    val otherUserName: String,

    val otherUserAvatar: String?,

    val lastMessage: String,

    val lastMessageAt: String?,

    val unreadCount: Int,

    val createdAt: String
)