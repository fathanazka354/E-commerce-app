package com.fathan.e_commerce.features.chat.data.model.response

import com.fathan.e_commerce.features.chat.domain.entity.MessageType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class ConversationResponse(
    @SerialName("room_id")
    val roomId: String,

    @SerialName("room_created_at")
    val roomCreatedAt: String,

    // Product Info
    @SerialName("product_id")
    val productId: Int? = null,

    @SerialName("product_title")
    val productTitle: String? = null,

    @SerialName("product_price")
    val productPrice: Long? = null,

    @SerialName("product_image")
    val productImage: String? = null,

    // Other User (Lawan Bicara)
    @SerialName("other_user_name")
    val otherUserName: String? = null,

    @SerialName("other_user_avatar")
    val otherUserAvatar: String? = null,

    @SerialName("other_user_email")
    val otherUserEmail: String? = null,

    @SerialName("other_user_auth_id")
    val otherUserAuthId: String,

    // Last Message
    @SerialName("last_message_id")
    val lastMessageId: String? = null,

    @SerialName("last_message")
    val lastMessage: String? = null,

    @SerialName("last_message_type")
    val lastMessageType: String? = null, // "text", "image", "audio"

    @SerialName("last_sender_id")
    val lastSenderId: String? = null,

    @SerialName("last_message_time")
    val lastMessageTime: String? = null,

    @SerialName("last_sender_name")
    val lastSenderName: String? = null,

    // Read Status
    @SerialName("is_read")
    val isRead: Boolean = false,

    @SerialName("unread_count")
    val unreadCount: Int = 0
)