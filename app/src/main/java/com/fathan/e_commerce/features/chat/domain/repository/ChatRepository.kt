package com.fathan.e_commerce.features.chat.domain.repository

import android.net.Uri
import com.fathan.e_commerce.features.chat.domain.entity.ConversationItem
import com.fathan.e_commerce.features.chat.domain.entity.Message
import io.github.jan.supabase.realtime.RealtimeChannel

interface ChatRepository {

    suspend fun getCurrentUserId(): String?
    suspend fun fetchAllChats(): Result<List<ConversationItem>>
    suspend fun getMessages(conversationId: String): Result<List<Message>>

    suspend fun sendMessage(
        conversationId: String,
        messageType: String,
        messageContent: String,
        productId: Long? = null
    ): Result<String>

    suspend fun sendImage(
        conversationId: String,
        imageUri: Uri
    ): Result<String>

    suspend fun sendAudio(
        conversationId: String,
        audioUri: Uri,
        duration: Long
    ): Result<String>

    suspend fun markConversationAsRead(conversationId: String): Result<Unit>

    suspend fun createOrGetConversation(
        buyerId: String,
        sellerId: String
    ): Result<String>

    fun subscribeToMessages(
        conversationId: String,
        onNewMessage: (Message) -> Unit
    ): RealtimeChannel

}