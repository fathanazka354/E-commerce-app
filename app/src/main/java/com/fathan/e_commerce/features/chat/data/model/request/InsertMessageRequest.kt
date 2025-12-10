package com.fathan.e_commerce.features.chat.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class InsertMessageRequest(
    val room_id: String,
    val sender_id: String,
    val content: String = "",
    val message_type: String = "text",
    val media_url: String? = null,
    val metadata: Map<String, String>? = null,
    val is_read: Boolean? = null
)