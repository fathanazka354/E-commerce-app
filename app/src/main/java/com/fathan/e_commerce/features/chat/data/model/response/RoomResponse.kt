package com.fathan.e_commerce.features.chat.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class RoomResponse(
    val id: String,
    val title: String?,
    val is_group: Boolean,
    val created_by: String,
    val created_at: String
)
