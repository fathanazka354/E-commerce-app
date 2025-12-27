package com.fathan.e_commerce.features.chat.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MessageResponse(
    @SerialName("message_id")
    val messageId: String? = null,

    @SerialName("id")
    val id: String? = null,
    @SerialName("room_id")
    val roomId: String,
    @SerialName("sender_id")
    val senderId: String,
    val content: String? = null,
    @SerialName("message_type")
    val messageType: String,
    @SerialName("media_url")
    val mediaUrl: String? = null,
    val metadata: JsonElement? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("product_id")
    val productId: Int? = null,
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("other_user_name")
    val otherUserName: String? = null,
)
