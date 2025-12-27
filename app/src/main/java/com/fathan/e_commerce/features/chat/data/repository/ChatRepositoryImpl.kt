package com.fathan.e_commerce.features.chat.data.repository

import android.net.Uri
import android.util.Log
import com.fathan.e_commerce.data.utils.toDomain
import com.fathan.e_commerce.features.chat.data.model.request.InsertMessageRequest
import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
import com.fathan.e_commerce.features.chat.data.model.response.MessageWithReadStatus
import com.fathan.e_commerce.features.chat.data.model.response.toStringMap
import com.fathan.e_commerce.features.chat.data.source.SupabaseRemoteDataSource
import com.fathan.e_commerce.features.chat.domain.entity.ChatMessage
import com.fathan.e_commerce.features.chat.domain.entity.Conversation
import com.fathan.e_commerce.features.chat.domain.entity.MessageType
import com.fathan.e_commerce.features.chat.domain.repository.ChatRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ChatRepositoryImpl(
    private val remote: SupabaseRemoteDataSource,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val supabase: SupabaseClient
) : ChatRepository {

    override val incomingMessages: Flow<ChatMessage> =
        remote.incomingMessagesRaw.map { it.toDomain() }

    init {
        remote.startRealtime()
        scope.launch {
            remote.incomingMessagesRaw.collect { raw ->
                // Log for debug if needed
            }
        }
    }

    override suspend fun fetchAllChats(): List<Conversation> {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")

        return remote.fetchConversations()
            .map { it.toDomain(userId) }
    }

    override suspend fun createRoomIfNotExists(targetUserId: String): String {
        val currentUserId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")

        val room = remote.createRoomIfNotExists(currentUserId, targetUserId)
        return room.id
    }

    override suspend fun fetchChatByRoomId(roomId: String): List<ChatMessage> {
        val messages = remote.fetchMessagesByRoom(roomId)
            .map { it.toDomain() }
        val currentUserId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")

        return messages.map { msg ->
            if (msg.type == MessageType.IMAGE || msg.type == MessageType.AUDIO) {
                val mediaUrl = msg.mediaUrl

                val signedUrl = if (mediaUrl != null && !mediaUrl.startsWith("http")) {
                    Log.d("ChatRepo", "Generating signed URL for path: $mediaUrl")
                    remote.createSignedUrl("chat-media", mediaUrl, 60 * 60 * 24)
                } else {
                    Log.d("ChatRepo", "Using existing URL: $mediaUrl")
                    mediaUrl
                }

                    Log.d("ChatRepo", "isMe = ${currentUserId == msg.senderId} IsRead: ${msg.isRead}")

                msg.copy(mediaUrl = signedUrl, isMe = currentUserId == msg.senderId, )
            } else {
                    Log.d("ChatRepo", "isMe = ${currentUserId == msg.senderId} IsRead: ${msg.isRead}")
                msg.copy(isMe = currentUserId == msg.senderId)
            }
        }
    }

    override suspend fun fetchChatByRoomIdWithStatus(
        roomId: String
    ): List<ChatMessage> {

        val signedUrlCache = mutableMapOf<String, String?>()
        val currentUserId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")
        return remote.fetchMessagesWithReadStatus(roomId).map { msg ->
            val type = MessageType.from(msg.messageType)

            val finalMediaUrl = msg.mediaUrl?.let { path ->
                signedUrlCache.getOrPut(path) {
                    if (path.startsWith("http")) path
                    else remote.createSignedUrl("chat-media", path, 60 * 60 * 24)
                }
            }

            ChatMessage(
                id = msg.messageId,
                roomId = msg.roomId,
                senderId = msg.senderId,
                senderName = msg.senderName,
                otherUserName = msg.buyerName,

                type = type,
                message = msg.content,
                mediaUrl = finalMediaUrl,
                createdAt = msg.createdAt,

                isMe = msg.senderId == currentUserId,
                isRead = msg.isRead,

                imageUri =
                    if (type == MessageType.IMAGE) finalMediaUrl?.let(Uri::parse) else null,

                audioUri =
                    if (type == MessageType.AUDIO) finalMediaUrl?.let(Uri::parse) else null,

                // ✅ convert JsonElement → Map di UI layer
                metadata = msg.metadata?.toStringMap()
            )
        }
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

    override suspend fun findChat(query: String, senderId: String?): List<ChatMessage> {
        val list = remote.searchMessagesByContentOrSender(query, senderId)
        return list.map { mapRawToDomain(it) }
    }

    override suspend fun sendText(
        roomId: String,
        text: String
    ): ChatMessage? {

        val currentUserId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")

        val payload = InsertMessageRequest(
            roomId = roomId,
            content = text,
            messageType = "text",
            senderId = currentUserId
        )

        val inserted = remote.insertMessage(payload)
        return inserted.toDomain()
    }

    override suspend fun sendImage(
        roomId: String,
        senderId: String,
        bytes: ByteArray,
        mime: String,
        filename: String,
        caption: String?
    ): ChatMessage? {

        val currentUserId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")

        val path = "$roomId/${System.currentTimeMillis()}_$filename"

        Log.d("ChatRepo", "Uploading image with path: $path")

        remote.uploadFile("chat-media", path, bytes, mime)
        val url = remote.createSignedUrl("chat-media", path, 60 * 60 * 24 * 365)

        Log.d("ChatRepo", "Generated signed URL: $url")

        val payload = InsertMessageRequest(
            roomId = roomId,
            senderId = currentUserId,
            content = caption ?: "",
            messageType = "image",
            mediaUrl = url
        )

        val inserted = remote.insertMessage(payload)
        return inserted.toDomain()
    }

    override suspend fun sendAudio(
        roomId: String,
        bytes: ByteArray,
        mime: String,
        filename: String,
        durationMs: Long
    ): ChatMessage? {

//        val currentUserId = supabase.auth.currentUserOrNull()?.id
//            ?: throw IllegalStateException("User not logged in")

        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")

        val path = "$roomId/${System.currentTimeMillis()}_$filename"

        Log.d("ChatRepo", "Uploading audio with path: $path")

        remote.uploadFile("chat-media", path, bytes, mime)
        val url = remote.createSignedUrl("chat-media", path, 60 * 60 * 24 * 365)

        Log.d("ChatRepo", "Generated signed URL: $url")

        val payload = InsertMessageRequest(
            roomId = roomId,
            senderId = userId,
            content = "",
            messageType = "audio",
            mediaUrl = url,
            metadata = Json.parseToJsonElement("""
                {
                    "duration_ms": $durationMs
                }
            """.trimIndent())
        )

        val inserted = remote.insertMessage(payload)
        return inserted.toDomain()
    }

    private fun mapRawToDomain(raw: MessageResponse): ChatMessage {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")

        return ChatMessage(
            id = raw.messageId?: raw.id?:"",
            roomId = raw.roomId,
            senderId = raw.senderId,
            message = raw.content,
            type = MessageType.from(raw.messageType),
            mediaUrl = raw.mediaUrl,
            createdAt = raw.createdAt,
            isRead = false,
            metadata = null,
            isMe = raw.senderId == userId
        )
    }
}