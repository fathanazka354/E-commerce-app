package com.fathan.e_commerce.features.chat.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Message.kt
@Serializable
data class MessageResponse(
    @SerialName("id")  // ✅ ADD THIS
    val id: String,

    @SerialName("conversation_id")
    val conversationId: String,

    @SerialName("sender_id")
    val senderId: String,

    @SerialName("message_type")
    val messageType: String, // "text", "image", "product_card", "system"

    @SerialName("content")
    val messageContent: String,

    @SerialName("product_id")
    val productId: Long?,

    @SerialName("is_read")
    val isRead: Boolean,

    @SerialName("read_at")
    val readAt: String?,

    @SerialName("created_at")
    val createdAt: String,

)