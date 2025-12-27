package com.fathan.e_commerce.features.chat.data.model.response

@kotlinx.serialization.Serializable
data class RoomMemberRow(
    val id: String,
    val room_id: String,
    val user_id: String
)
