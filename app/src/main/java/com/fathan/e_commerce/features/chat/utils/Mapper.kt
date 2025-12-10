//package com.fathan.e_commerce.features.chat.utils
//import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
//import com.fathan.e_commerce.features.chat.domain.entity.Message
//
//fun MessageResponse.toDomain(): Message {
//    return Message(
//        id = id,
//        roomId = room_id,
//        senderId = sender_id,
//        content = content,
//        messageType = message_type,
//        mediaUrl = media_url,
//        createdAt = created_at
//    )
//}