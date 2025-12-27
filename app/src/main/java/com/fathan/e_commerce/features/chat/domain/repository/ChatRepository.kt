package com.fathan.e_commerce.features.chat.domain.repository

import com.fathan.e_commerce.features.chat.domain.entity.ChatMessage
import com.fathan.e_commerce.features.chat.domain.entity.Conversation
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    val incomingMessages: Flow<ChatMessage>

    suspend fun fetchAllChats(): List<Conversation>


    suspend fun createRoomIfNotExists(targetUserId: String): String

    suspend fun fetchChatByRoomId(roomId: String): List<ChatMessage>
    suspend fun fetchChatByRoomIdWithStatus(roomId: String): List<ChatMessage>

    suspend fun readByRoomId(roomId: String)

    suspend fun markAllAsRead(receiverId: String)

    suspend fun deleteChat(messageId: String)

    suspend fun findChat(query: String, senderId: String?): List<ChatMessage>

    // ✅ Return ChatMessage after insert
    suspend fun sendText(roomId: String, text: String): ChatMessage?

    // ✅ Return ChatMessage after insert
    suspend fun sendImage(
        roomId: String,
        senderId: String,
        bytes: ByteArray,
        mime: String,
        filename: String,
        caption: String?
    ): ChatMessage?

    // ✅ Return ChatMessage after insert
    suspend fun sendAudio(
        roomId: String,
        bytes: ByteArray,
        mime: String,
        filename: String,
        durationMs: Long
    ): ChatMessage?
}