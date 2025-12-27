package com.fathan.e_commerce.features.chat.domain.repository

import com.fathan.e_commerce.features.chat.domain.entity.ConversationItem
import com.fathan.e_commerce.features.chat.domain.entity.Message
import io.github.jan.supabase.realtime.RealtimeChannel

interface ChatRepository {


    suspend fun fetchAllChats(): Result<List<ConversationItem>>
    suspend fun getMessages(conversationId: String): Result<List<Message>>

    suspend fun sendMessage(
        conversationId: String,
        messageType: String,
        messageContent: String,
        productId: Long? = null
    ): Result<String>

    suspend fun markConversationAsRead(conversationId: String): Result<Unit>

    suspend fun createOrGetConversation(
        buyerId: String,
        sellerId: String
    ): Result<String>

    suspend fun subscribeToMessages(
        conversationId: String,
        onNewMessage: (Message) -> Unit
    ): RealtimeChannel

//
//    suspend fun createRoomIfNotExists(targetUserId: String): String
//
//    suspend fun fetchChatByRoomId(roomId: String): List<ChatMessage>
//    suspend fun fetchChatByRoomIdWithStatus(roomId: String): List<ChatMessage>
//
//    suspend fun readByRoomId(roomId: String)
//
//    suspend fun markAllAsRead(receiverId: String)
//
//    suspend fun deleteChat(messageId: String)
//
//    suspend fun findChat(query: String, senderId: String?): List<ChatMessage>
//
//    // ✅ Return ChatMessage after insert
//    suspend fun sendText(roomId: String, text: String): ChatMessage?
//
//    // ✅ Return ChatMessage after insert
//    suspend fun sendImage(
//        roomId: String,
//        senderId: String,
//        bytes: ByteArray,
//        mime: String,
//        filename: String,
//        caption: String?
//    ): ChatMessage?
//
//    // ✅ Return ChatMessage after insert
//    suspend fun sendAudio(
//        roomId: String,
//        bytes: ByteArray,
//        mime: String,
//        filename: String,
//        durationMs: Long
//    ): ChatMessage?
}