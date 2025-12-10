package com.fathan.e_commerce.features.chat.data.repository

import com.fathan.e_commerce.features.chat.data.model.request.InsertMessageRequest
import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
import com.fathan.e_commerce.features.chat.data.source.SupabaseRemoteDataSource
import com.fathan.e_commerce.features.chat.domain.entity.ChatMessage
import com.fathan.e_commerce.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ChatRepositoryImpl(
    private val remote: SupabaseRemoteDataSource,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : ChatRepository {

    private val _incoming = MutableSharedFlow<ChatMessage>(replay = 50)
    override val incomingMessages: Flow<ChatMessage> = _incoming.asSharedFlow()

    init {
        // subscribe remote messages raw -> map to domain and emit
        remote.startRealtime()
        scope.launch {
            remote.incomingMessagesRaw.collect { raw ->
                val mapped = mapRawToDomain(raw)
                _incoming.emit(mapped)
            }
        }
    }

    override suspend fun fetchAllChats(): List<ChatMessage> {
        val list = remote.fetchAllMessages()
        return list.map { mapRawToDomain(it) }
    }

    override suspend fun fetchChatByRoomId(roomId: String): List<ChatMessage> {
        val list = remote.fetchMessagesByRoom(roomId)
        return list.map { mapRawToDomain(it) }
    }

    override suspend fun readByRoomId(roomId: String) {
        remote.markReadByRoom(roomId)
    }

    override suspend fun markAllAsRead(receiverId: String) {
        remote.markAllAsReadForReceiver(receiverId)
    }

    override suspend fun deleteChat(messageId: String) {
        remote.deleteMessageById(messageId)
    }

    override suspend fun findChat(query: String, senderId: String?) : List<ChatMessage> {
        val list = remote.searchMessagesByContentOrSender(query, senderId)
        return list.map { mapRawToDomain(it) }
    }

    override suspend fun sendText(roomId: String, senderId: String, text: String) {
        val payload = InsertMessageRequest(
            room_id = roomId,
            sender_id = senderId,
            content = text,
            message_type = "text"
        )
        remote.insertMessage(payload)
    }

    override suspend fun sendImage(roomId: String, senderId: String, bytes: ByteArray, mime: String, filename: String, caption: String?) {
        val path = "room-$roomId/${System.currentTimeMillis()}_$filename"
        remote.uploadFile("chat-media", path, bytes, mime)
        val url = remote.createSignedUrl("chat-media", path, 60 * 60 * 24 * 7) // 7 days
        val payload = InsertMessageRequest(
            room_id = roomId,
            sender_id = senderId,
            content = caption ?: "",
            message_type = "image",
            media_url = url
        )
        remote.insertMessage(payload)
    }

    override suspend fun sendAudio(roomId: String, senderId: String, bytes: ByteArray, mime: String, filename: String, durationMs: Long) {
        val path = "room-$roomId/${System.currentTimeMillis()}_$filename"
        remote.uploadFile("chat-media", path, bytes, mime)
        val url = remote.createSignedUrl("chat-media", path, 60 * 60 * 24 * 7)
        val payload = InsertMessageRequest(
            room_id = roomId,
            sender_id = senderId,
            content = "",
            message_type = "audio",
            media_url = url,
            metadata = mapOf("duration_ms" to durationMs.toString())
        )
        remote.insertMessage(payload)
    }

    private fun mapRawToDomain(raw: MessageResponse): ChatMessage {
        return ChatMessage(
            id = raw.id,
            roomId = raw.room_id,
            senderId = raw.sender_id,
            content = raw.content,
            messageType = raw.message_type ?: "text",
            mediaUrl = raw.media_url,
            metadata = raw.metadata?.toMap()?.mapValues { it.value.toString() } ?: emptyMap(),
            isRead = raw.is_read ?: false,
            createdAt = raw.created_at
        )
    }
}