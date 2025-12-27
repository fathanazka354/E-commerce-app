package com.fathan.e_commerce.features.chat.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class RoomUi(
    val room_id: String,
    val title: String? = null,
    val is_group: Boolean = false,
    val last_message: String? = null,
    val last_sender: String? = null,
    val last_message_time: String? = null,
    val unread_count: Int = 0,
)
