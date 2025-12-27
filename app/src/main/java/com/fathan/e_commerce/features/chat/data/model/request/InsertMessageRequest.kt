package com.fathan.e_commerce.features.chat.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class InsertMessageRequest(
    @SerialName("room_id")
    val roomId: String,

    @SerialName("sender_id")
    val senderId: String,

    @SerialName("content")
    val content: String? = null,

    @SerialName("message_type")
    val messageType: String = "text",

    @SerialName("media_url")
    val mediaUrl: String? = null,

    @SerialName("metadata")
    val metadata: JsonElement? = null,
    val is_read: Boolean? = null
)