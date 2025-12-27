package com.fathan.e_commerce.features.chat.data.source

import com.fathan.e_commerce.features.chat.data.model.response.ConversationItemResponse
import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

interface ChatRemoteDataSource{
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
    suspend fun subscribeToMessages(
        conversationId: String,
        onNewMessage: (MessageResponse) -> Unit
    ): RealtimeChannel
}

class ChatRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient,
): ChatRemoteDataSource {
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

            val userId =
                supabaseClient.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")

            val response = supabaseClient.postgrest
                .rpc("send_message", parameters = mapOf(
                    "p_conversation_id" to conversationId,
                    "p_sender_id" to userId,
                    "p_message_type" to messageType,
                    "p_message_content" to messageContent,
                    if (productId != null){
                        "p_product_id" to productId
                    } else {
                        null to null
                    }
                )).decodeSingleOrNull<String>()

            Result.success(response ?: throw Exception("Response not found"))
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun markConversationAsRead(conversationId: String): Result<Unit> {
        return try {
            val userId =
                supabaseClient.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")

            supabaseClient.postgrest
                .rpc("mark_conversation_as_read") {
                    mapOf(
                        "p_conversation_id" to conversationId,
                        "p_user_id" to userId
                    )
                }
                .decodeSingleOrNull<Unit>()
            Result.success(Unit)
        } catch (e: Exception){
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

    override suspend fun subscribeToMessages(
        conversationId: String,
        onNewMessage: (MessageResponse) -> Unit
    ): RealtimeChannel {
        val channel = supabaseClient.realtime.channel("messages:$conversationId")

        CoroutineScope(Dispatchers.IO).launch {
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "messages"
                filter = "conversation_id=eq.$conversationId"
            }.collect { change ->
                when (change) {
                    is PostgresAction.Insert -> {
                        // Decode JsonObject ke Message
                        val message = decodeMessage(change.record)
                        onNewMessage(message)
                    }
                    is PostgresAction.Update -> {
                        // Optional: val updatedMessage = decodeMessage(change.record)
                    }
                    is PostgresAction.Delete -> {
                        // Optional: val deletedId = change.oldRecord["id"]?.toString()
                    }
                    else -> {}
                }
            }
        }

        channel.subscribe()
        return channel
    }

    // Helper function untuk decode JsonObject ke Message
    private fun decodeMessage(jsonObject: JsonObject): MessageResponse {
        return MessageResponse(
            id = jsonObject["id"]?.jsonPrimitive?.content ?: "",
            conversationId = jsonObject["conversation_id"]?.jsonPrimitive?.content ?: "",
            senderId = jsonObject["sender_id"]?.jsonPrimitive?.content ?: "",
            messageType = jsonObject["message_type"]?.jsonPrimitive?.content ?: "text",
            messageContent = jsonObject["message_content"]?.jsonPrimitive?.content ?: "",
            productId = jsonObject["product_id"]?.jsonPrimitive?.longOrNull,
            isRead = jsonObject["is_read"]?.jsonPrimitive?.booleanOrNull ?: false,
            readAt = jsonObject["read_at"]?.jsonPrimitive?.contentOrNull,
            createdAt = jsonObject["created_at"]?.jsonPrimitive?.content ?: "",
            updatedAt = jsonObject["updated_at"]?.jsonPrimitive?.content ?: ""
        )
    }
}
