package com.fathan.e_commerce.features.chat.utils

import android.util.Log
import com.fathan.e_commerce.features.chat.data.model.response.ConversationItemResponse
import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
import com.fathan.e_commerce.features.chat.domain.entity.ConversationItem
import com.fathan.e_commerce.features.chat.domain.entity.Message

fun MessageResponse.toEntity(): Message {
    Log.d("ChatMapper", "Converting MessageResponse to Message:")
    Log.d("ChatMapper", "  ID: $id")
    Log.d("ChatMapper", "  Content: '$messageContent'")
    Log.d("ChatMapper", "  Content length: ${messageContent.length}")
    return Message(
        id = id,
        senderId = senderId,
        messageType = messageType,
        conversationId = conversationId,
        createdAt = createdAt,
        isRead = isRead,
        readAt = readAt,
        productId = productId,
        messageContent = messageContent,
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
        otherUserAvatar = otherUserAvatar,
        messageType = messageType
    )
}