package com.fathan.e_commerce.features.chat.utils

import com.fathan.e_commerce.features.chat.data.model.response.ConversationItemResponse
import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
import com.fathan.e_commerce.features.chat.domain.entity.ConversationItem
import com.fathan.e_commerce.features.chat.domain.entity.Message

//import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
//import com.fathan.e_commerce.features.chat.domain.entity.Message
//
fun MessageResponse.toEntity(): Message {
    return Message(
        id = id,
        senderId = senderId,
        messageType = messageType,
        conversationId = conversationId,
        createdAt = createdAt,
        isRead = isRead,
        readAt = readAt,
        productId = productId,
        updatedAt = updatedAt,
        messageContent = messageContent
    )
}
fun ConversationItemResponse.toEntity(): ConversationItem {
    return ConversationItem(
        createdAt = createdAt,
        otherUserName = otherUserName,
        sellerId = sellerId,
        buyerId = buyerId,
        lastMessage = lastMessage,
        otherUserId = otherUserId,
        unreadCount = unreadCount,
        lastMessageAt = lastMessageAt,
        conversationId = conversationId,
        otherUserAvatar = otherUserAvatar
    )
}