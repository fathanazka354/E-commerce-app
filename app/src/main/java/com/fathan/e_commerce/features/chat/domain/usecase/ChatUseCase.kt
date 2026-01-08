package com.fathan.e_commerce.features.chat.domain.usecase

import android.net.Uri
import com.fathan.e_commerce.features.chat.domain.entity.ConversationItem
import com.fathan.e_commerce.features.chat.domain.entity.Message
import com.fathan.e_commerce.features.chat.domain.repository.ChatRepository


class FetchAllChats(private val repo: ChatRepository) {
    suspend operator fun invoke(): Result<List<ConversationItem>> = repo.fetchAllChats()
}
class FetchMessages(private val repo: ChatRepository) {
    suspend operator fun invoke(conversationId: String): Result<List<Message>> = repo.getMessages(conversationId)
}
class SendText(private val repo: ChatRepository) {
    suspend operator fun invoke(conversationId: String, messageType: String, messageContent: String, productId: Long?) = repo.sendMessage(conversationId,  messageType = messageType, messageContent = messageContent, productId = productId)
}
class SendImage(private val repo: ChatRepository) {
    suspend operator fun invoke(
        conversationId: String,
        imageUri: Uri
    ): Result<String> = repo.sendImage(conversationId, imageUri)
}

// ✅ NEW: Send Audio
class SendAudio(private val repo: ChatRepository) {
    suspend operator fun invoke(
        conversationId: String,
        audioUri: Uri,
        duration: Long
    ): Result<String> = repo.sendAudio(conversationId, audioUri, duration)
}

class Subscribe(private val repo: ChatRepository) {
    operator fun invoke(conversationId: String, onMessage: (Message) -> Unit) = repo.subscribeToMessages(conversationId = conversationId, onNewMessage = onMessage)
}
class MarkAsRead(private val repo: ChatRepository) {
    suspend operator fun invoke(conversationId: String) = repo.markConversationAsRead(conversationId = conversationId,)
}
data class ChatUseCases(
    val fetchAllChats: FetchAllChats,
    val fetchMessages: FetchMessages,
    val sendText: SendText,
    val subscribe: Subscribe,
    val markAsRead: MarkAsRead,
    val sendAudio: SendAudio,
    val sendImage: SendImage,
)
