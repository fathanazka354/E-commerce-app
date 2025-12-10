package com.fathan.e_commerce.features.chat.domain.usecase

import com.fathan.e_commerce.features.chat.domain.entity.ChatMessage
import com.fathan.e_commerce.features.chat.domain.repository.ChatRepository


import kotlinx.coroutines.flow.Flow

class FetchAllChats(private val repo: ChatRepository) {
    suspend operator fun invoke(): List<ChatMessage> = repo.fetchAllChats()
}
class FetchChatByRoom(private val repo: ChatRepository) {
    suspend operator fun invoke(roomId: String): List<ChatMessage> = repo.fetchChatByRoomId(roomId)
}
class ReadByRoom(private val repo: ChatRepository) {
    suspend operator fun invoke(roomId: String) = repo.readByRoomId(roomId)
}
class MarkAllAsRead(private val repo: ChatRepository) {
    suspend operator fun invoke(receiverId: String) = repo.markAllAsRead(receiverId)
}
class DeleteChat(private val repo: ChatRepository) {
    suspend operator fun invoke(messageId: String) = repo.deleteChat(messageId)
}
class FindChat(private val repo: ChatRepository) {
    suspend operator fun invoke(query: String, senderId: String? = null): List<ChatMessage> =
        repo.findChat(query, senderId)
}

class SendText(private val repo: ChatRepository) {
    suspend operator fun invoke(roomId: String, senderId: String, text: String) = repo.sendText(roomId, senderId, text)
}
class SendImage(private val repo: ChatRepository) {
    suspend operator fun invoke(roomId: String, senderId: String, bytes: ByteArray, mime: String, filename: String, caption: String?) =
        repo.sendImage(roomId, senderId, bytes, mime, filename, caption)
}
class SendAudio(private val repo: ChatRepository) {
    suspend operator fun invoke(roomId: String, senderId: String, bytes: ByteArray, mime: String, filename: String, durationMs: Long) =
        repo.sendAudio(roomId, senderId, bytes, mime, filename, durationMs)
}

data class ChatUseCases(
    val fetchAllChats: FetchAllChats,
    val fetchChatByRoom: FetchChatByRoom,
    val readByRoom: ReadByRoom,
    val markAllAsRead: MarkAllAsRead,
    val deleteChat: DeleteChat,
    val findChat: FindChat,
    val sendText: SendText,
    val sendImage: SendImage,
    val sendAudio: SendAudio,

    // expose incoming realtime messages for ViewModel to subscribe
    val incomingMessages: Flow<ChatMessage>
)
