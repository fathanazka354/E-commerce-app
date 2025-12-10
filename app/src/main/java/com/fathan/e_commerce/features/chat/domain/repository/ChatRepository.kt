package com.fathan.e_commerce.features.chat.domain.repository

import com.fathan.e_commerce.features.chat.domain.entity.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    val incomingMessages: Flow<ChatMessage>

    suspend fun fetchAllChats(): List<ChatMessage> // all messages / you can aggregate per room in UI
    suspend fun fetchChatByRoomId(roomId: String): List<ChatMessage>
    suspend fun readByRoomId(roomId: String): Unit
    suspend fun markAllAsRead(receiverId: String): Unit
    suspend fun deleteChat(messageId: String): Unit
    suspend fun findChat(query: String, senderId: String? = null): List<ChatMessage>

    // send
    suspend fun sendText(roomId: String, senderId: String, text: String)
    suspend fun sendImage(roomId: String, senderId: String, bytes: ByteArray, mime: String, filename: String, caption: String?)
    suspend fun sendAudio(roomId: String, senderId: String, bytes: ByteArray, mime: String, filename: String, durationMs: Long)
}