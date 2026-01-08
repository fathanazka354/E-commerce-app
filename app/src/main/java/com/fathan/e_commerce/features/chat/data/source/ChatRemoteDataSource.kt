    package com.fathan.e_commerce.features.chat.data.source

    import android.content.Context
    import android.net.Uri
    import android.util.Log
    import com.fathan.e_commerce.features.chat.data.model.request.SendMessageRequest
    import com.fathan.e_commerce.features.chat.data.model.response.ConversationItemResponse
    import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
    import dagger.hilt.android.qualifiers.ApplicationContext
    import io.github.jan.supabase.SupabaseClient
    import io.github.jan.supabase.gotrue.auth
    import io.github.jan.supabase.postgrest.postgrest
    import io.github.jan.supabase.postgrest.query.Order
    import io.github.jan.supabase.postgrest.rpc
    import io.github.jan.supabase.realtime.PostgresAction
    import io.github.jan.supabase.realtime.RealtimeChannel
    import io.github.jan.supabase.realtime.channel
    import io.github.jan.supabase.realtime.postgresChangeFlow
    import io.github.jan.supabase.realtime.realtime
    import io.github.jan.supabase.storage.storage
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.SupervisorJob
    import kotlinx.coroutines.launch
    import kotlinx.serialization.json.JsonObject
    import kotlinx.serialization.json.booleanOrNull
    import kotlinx.serialization.json.buildJsonObject
    import kotlinx.serialization.json.contentOrNull
    import kotlinx.serialization.json.jsonPrimitive
    import kotlinx.serialization.json.longOrNull
    import kotlinx.serialization.json.put
    import java.util.UUID
    import javax.inject.Inject
    import kotlin.time.Duration
    import kotlin.time.Duration.Companion.INFINITE

    interface ChatRemoteDataSource{
        suspend fun getCurrentUserId(): String?
        suspend fun getConversations(): List<ConversationItemResponse>
        suspend fun getMessages(conversationId: String): List<MessageResponse>
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
        fun subscribeToMessages(
            conversationId: String,
            onNewMessage: (MessageResponse) -> Unit
        ): RealtimeChannel

        suspend fun sendImage(
            conversationId: String,
            imageUri: Uri
        ): Result<String>

        // ✅ NEW
        suspend fun sendAudio(
            conversationId: String,
            audioUri: Uri,
            duration: Long
        ): Result<String>
    }

    class ChatRemoteDataSourceImpl @Inject constructor(
        private val supabaseClient: SupabaseClient,
        @ApplicationContext private val context: Context
    ): ChatRemoteDataSource {
        private val realtimeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private var cachedUserId: String? = null

        override suspend fun getCurrentUserId(): String? {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                cachedUserId = userId  // ✅ Cache it
            }
            return userId ?: cachedUserId  // ✅ Return cached if current is null
        }

        override suspend fun getConversations(): List<ConversationItemResponse> {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
            val params = mapOf(
                "user_id_param" to userId
            )
            return supabaseClient.postgrest
                    .rpc("get_user_conversations", parameters = params).decodeList<ConversationItemResponse>()
        }

        override suspend fun getMessages(conversationId: String): List<MessageResponse> {
            val response = supabaseClient.postgrest["messages"]
                .select {
                    filter {
                        eq("conversation_id", conversationId)
                    }
                    order("created_at", Order.ASCENDING)
                }.decodeList<MessageResponse>()

            return response
        }

        override suspend fun sendMessage(
            conversationId: String,
            messageType: String,
            messageContent: String,
            productId: Long?
        ): Result<String> {
            return try {
                val userId = getCurrentUserId()
                    ?: throw Exception("User not logged in and no cached user ID")

                val params = SendMessageRequest(
                    conversationId = conversationId,
                    senderId = userId,
                    messageType = messageType,
                    content = messageContent,
                    productId = productId
                )

                Log.d("ChatRemoteDataSource", "Sending: $params")

                val response = supabaseClient.postgrest
                    .rpc("send_message", parameters = params)
                    .decodeAs<String>()

                Log.d("ChatRemoteDataSource", "Sent: $response")
                Result.success(response)

            } catch (e: Exception) {
                Log.e("ChatRemoteDataSource", "Error: ${e.message}", e)
                Result.failure(e)
            }
        }

        override suspend fun markConversationAsRead(conversationId: String): Result<Unit> {
            return try {
                val userId = getCurrentUserId()
                    ?: throw Exception("User not logged in and no cached user ID")
                // ✅ Call RPC without decoding response (VOID function returns nothing)
                supabaseClient.postgrest
                    .rpc("mark_conversation_as_read", parameters = mapOf(
                        "p_conversation_id" to conversationId,
                        "p_user_id" to userId
                    ))

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ChatRemoteDataSource", "Error marking as read", e)
                Result.failure(e)
            }
        }

        override suspend fun createOrGetConversation(
            buyerId: String,
            sellerId: String
        ): Result<String> {
            return try {
                val conversationId = supabaseClient.postgrest
                    .rpc("create_or_get_conversation",){
                        mapOf(
                            "p_buyer_id" to buyerId,
                            "p_seller_id" to sellerId
                        )
                    }.decodeSingleOrNull<String>() ?: throw Exception("Conversation not found")

                Result.success(conversationId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        override fun subscribeToMessages(
            conversationId: String,
            onNewMessage: (MessageResponse) -> Unit
        ): RealtimeChannel {
            val channel = supabaseClient.realtime.channel("messages:$conversationId")

            // ✅ Launch collection in dedicated scope
            realtimeScope.launch {
                try {
                    channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                        table = "messages"
                        filter = "conversation_id=eq.$conversationId"
                    }.collect { change ->
                        when (change) {
                            is PostgresAction.Insert -> {
                                val message = decodeMessage(change.record)
                                onNewMessage(message)
                                Log.d("ChatRemoteDataSource", "New message received: ${message.id}")
                            }
                            is PostgresAction.Update -> {
                                // Optional: handle update
                                Log.d("ChatRemoteDataSource", "Message updated")
                            }
                            is PostgresAction.Delete -> {
                                // Optional: handle delete
                                Log.d("ChatRemoteDataSource", "Message deleted")
                            }
                            else -> {}
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatRemoteDataSource", "Error in realtime flow", e)
                }
            }

            // ✅ Launch subscribe in dedicated scope
            realtimeScope.launch {
                try {
                    channel.subscribe()
                    Log.d("ChatRemoteDataSource", "Subscribed to channel: messages:$conversationId")
                } catch (e: Exception) {
                    Log.e("ChatRemoteDataSource", "Failed to subscribe to channel", e)
                }
            }

            return channel
        }

        private fun decodeMessage(jsonObject: JsonObject): MessageResponse {
            return MessageResponse(
                id = jsonObject["id"]?.jsonPrimitive?.content ?: "",
                conversationId = jsonObject["conversation_id"]?.jsonPrimitive?.content ?: "",
                senderId = jsonObject["sender_id"]?.jsonPrimitive?.content ?: "",
                messageType = jsonObject["message_type"]?.jsonPrimitive?.content ?: "text",
                messageContent = jsonObject["content"]?.jsonPrimitive?.content ?: "",
                productId = jsonObject["product_id"]?.jsonPrimitive?.longOrNull,
                isRead = jsonObject["is_read"]?.jsonPrimitive?.booleanOrNull ?: false,
                readAt = jsonObject["read_at"]?.jsonPrimitive?.contentOrNull,
                createdAt = jsonObject["created_at"]?.jsonPrimitive?.content ?: "",
            )
        }

        // ✅ Send Image
        override suspend fun sendImage(
            conversationId: String,
            imageUri: Uri
        ): Result<String> {
            return try {
                val userId = getCurrentUserId()
                    ?: throw Exception("User not logged in and no cached user ID")

                Log.d(TAG, "Starting image upload for conversation: $conversationId")

                // Upload image to storage
                val imageUrl = uploadImageToStorage(imageUri)

                Log.d(TAG, "Image uploaded: $imageUrl")

                // ✅ Store as JSON for consistency
                val imageData = buildJsonObject {
                    put("url", imageUrl)
                    put("width", 0)  // Optional: add image dimensions if needed
                    put("height", 0)
                }.toString()

                // Send message with image data
                val params = SendMessageRequest(
                    conversationId = conversationId,
                    senderId = userId,
                    messageType = "image",
                    content = imageData,  // ✅ Store as JSON
                    productId = null
                )

                val response = supabaseClient.postgrest
                    .rpc("send_message", parameters = params)
                    .decodeAs<String>()

                Log.d(TAG, "Image message sent: $response")
                Result.success(response)

            } catch (e: Exception) {
                Log.e(TAG, "Error sending image", e)
                Result.failure(e)
            }
        }

        // ✅ Send Audio
        override suspend fun sendAudio(
            conversationId: String,
            audioUri: Uri,
            duration: Long
        ): Result<String> {
            return try {
                val userId = getCurrentUserId()
                    ?: throw Exception("User not logged in and no cached user ID")

                Log.d(TAG, "Starting audio upload for conversation: $conversationId")

                // Upload audio to storage
                val audioUrl = uploadAudioToStorage(audioUri)

                Log.d(TAG, "Audio uploaded: $audioUrl")

                // ✅ Store as JSON instead of pipe-separated
                val audioData = buildJsonObject {
                    put("url", audioUrl)
                    put("duration", duration)
                }.toString()

                // Send message with audio data
                val params = SendMessageRequest(
                    conversationId = conversationId,
                    senderId = userId,
                    messageType = "audio",
                    content = audioData,  // ✅ Store as JSON
                    productId = null
                )

                val response = supabaseClient.postgrest
                    .rpc("send_message", parameters = params)
                    .decodeAs<String>()

                Log.d(TAG, "Audio message sent: $response")
                Result.success(response)

            } catch (e: Exception) {
                Log.e(TAG, "Error sending audio", e)
                Result.failure(e)
            }
        }


        // ✅ Upload Image Helper
        // ✅ Upload Image with Signed URL
        private suspend fun uploadImageToStorage(imageUri: Uri): String {
            val fileName = "images/${UUID.randomUUID()}.jpg"

            val bytes = context.contentResolver.openInputStream(imageUri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read image file")

            Log.d(TAG, "Uploading image: $fileName, size: ${bytes.size} bytes")

            // Upload to storage
            supabaseClient.storage[STORAGE_BUCKET].upload(fileName, bytes)

            // ✅ Create signed URL (valid for 1 year)
            val signedUrl = supabaseClient.storage[STORAGE_BUCKET]
                .createSignedUrl(fileName, expiresIn = INFINITE.times(365 * 24 * 60 * 60)) // 1 year

            Log.d(TAG, "Image uploaded with signed URL: $signedUrl")

            return signedUrl
        }

        // ✅ Upload Audio with Signed URL
        private suspend fun uploadAudioToStorage(audioUri: Uri): String {
            val fileName = "audio/${UUID.randomUUID()}.m4a"

            val bytes = context.contentResolver.openInputStream(audioUri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read audio file")

            Log.d(TAG, "Uploading audio: $fileName, size: ${bytes.size} bytes")

            supabaseClient.storage[STORAGE_BUCKET].upload(fileName, bytes)

            // ✅ Create signed URL (valid for 1 year)
            val signedUrl = supabaseClient.storage[STORAGE_BUCKET]
                .createSignedUrl(fileName, expiresIn = Duration.INFINITE.times(365 * 24 * 60 * 60))

            Log.d(TAG, "Audio uploaded with signed URL: $signedUrl")

            return signedUrl
        }

        companion object {
            private const val STORAGE_BUCKET = "chat-media" // ✅ Bucket name
            private const val TAG = "ChatRemoteDataSource"
        }
    }
