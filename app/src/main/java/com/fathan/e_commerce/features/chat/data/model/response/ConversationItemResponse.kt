package com.fathan.e_commerce.features.chat.data.model.response

import com.fathan.e_commerce.features.chat.domain.entity.MessageType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// ConversationItem.kt
@Serializable
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

    @SerialName("message_type")
    val messageType: MessageType,

    @SerialName("last_message_at")
    val lastMessageAt: String?,

    @SerialName("unread_count")
    val unreadCount: Int,

    @SerialName("created_at")
    val createdAt: String
)
