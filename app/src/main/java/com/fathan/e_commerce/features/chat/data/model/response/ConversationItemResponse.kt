package com.fathan.e_commerce.features.chat.data.model.response

import kotlinx.serialization.SerialName

// ConversationItem.kt
data class ConversationItemResponse(
    @SerialName("conversation_id")
    val conversationId: String,

    @SerialName("buyer_id")
    val buyerId: String,

    @SerialName("seller_id")
    val sellerId: String,

    @SerialName("other_user_id")
    val otherUserId: String,

    @SerialName("other_user_name")
    val otherUserName: String,

    @SerialName("other_user_avatar")
    val otherUserAvatar: String?,

    @SerialName("last_message")
    val lastMessage: String,

    @SerialName("last_message_at")
    val lastMessageAt: String?,

    @SerialName("unread_count")
    val unreadCount: Int,

    @SerialName("created_at")
    val createdAt: String
)
