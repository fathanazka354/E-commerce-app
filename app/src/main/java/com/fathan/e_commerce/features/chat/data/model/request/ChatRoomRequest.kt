package com.fathan.e_commerce.features.chat.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomRequest(
    val title: String? = null,
    val is_group: Boolean = false,
    val created_by: String
)
