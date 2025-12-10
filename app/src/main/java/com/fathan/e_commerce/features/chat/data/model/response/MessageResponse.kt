package com.fathan.e_commerce.features.chat.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val id: String,
    val room_id: String,
    val sender_id: String,
    val content: String? = null,
    val message_type: String? = "text",
    val media_url: String? = null,
    val metadata: kotlinx.serialization.json.JsonObject? = null,
    val is_read: Boolean? = false,
    val created_at: String
)