package com.fathan.e_commerce.features.chat.data.repository

import android.util.Log
import com.fathan.e_commerce.data.utils.ErrorException
import com.fathan.e_commerce.data.utils.ErrorHandler
import com.fathan.e_commerce.data.utils.safeApiCall
import com.fathan.e_commerce.data.utils.validateConversationId
import com.fathan.e_commerce.data.utils.validateMessageContent
import com.fathan.e_commerce.data.utils.validateMessageType
import com.fathan.e_commerce.data.utils.validateUserId
import com.fathan.e_commerce.features.chat.data.source.ChatRemoteDataSource
import com.fathan.e_commerce.features.chat.domain.entity.ConversationItem
import com.fathan.e_commerce.features.chat.domain.entity.Message
import com.fathan.e_commerce.features.chat.domain.repository.ChatRepository
import com.fathan.e_commerce.features.chat.utils.toEntity
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepositoryImpl(
    private val remote: ChatRemoteDataSource,
) : ChatRepository {
    companion object {
        private const val TAG = "ChatRepositoryImpl"
        private const val MAX_MESSAGE_LENGTH = 4000
    }

    override suspend fun fetchAllChats(): Result<List<ConversationItem>> {
        return safeApiCall {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "fetchAllChats: all conversations")
                try {

                    val conversations = remote.getConversations()

                    if (conversations.isEmpty()) {
                        Log.d(TAG, "fetchAllChats: no found")
                        return@withContext emptyList()
                    }

                    val result = conversations.map { it.toEntity() }
                    result
                } catch (e: Exception) {
                    Log.e(TAG, "fetchAllChats: Error conversation", e)
                    throw ErrorHandler.handleException(e)
                }
            }
        }
    }

    override suspend fun getMessages(conversationId: String): Result<List<Message>> {
        return safeApiCall {
            withContext(Dispatchers.IO) {
                try {
                    conversationId.validateConversationId()

                    Log.d(TAG, "getMessages: Fetching messages for conversation: $conversationId")

                    val messages = remote.getMessages(conversationId)

                    if (messages.isEmpty()) {
                        Log.d(TAG, "No messages found in conversation: $conversationId")

                        return@withContext emptyList<Message>()
                    }

                    val result = messages.map { it.toEntity() }
                    Log.d(TAG, "Successfully fetched ${result.size} messages")
                    result
                } catch (e: ErrorException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching messages", e)
                    throw ErrorHandler.handleException(e)
                }
            }
        }
    }

    override suspend fun sendMessage(
        conversationId: String,
        messageType: String,
        messageContent: String,
        productId: Long?
    ): Result<String> {
        return safeApiCall {
            withContext(Dispatchers.IO) {
                try {

                    conversationId.validateConversationId()
                    messageType.validateMessageType()
                    messageContent.validateMessageContent(MAX_MESSAGE_LENGTH)

                    val result = remote.sendMessage(
                        conversationId = conversationId,
                        messageType = messageType,
                        messageContent = messageContent,
                        productId = productId
                    )

                    result.getOrElse { error ->
                        Log.e(TAG, "sendMessage: ", error)
                        throw error as? ErrorException ?: ErrorHandler.handleException(error)
                    }.also { messageId ->
                        Log.d(TAG, "sendMessage: $messageId")
                    }
                } catch (e: ErrorException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "sendMessage: ", e)
                    throw ErrorHandler.handleException(e)
                }
            }
        }
    }

    override suspend fun markConversationAsRead(conversationId: String): Result<Unit> {
        return safeApiCall {
            withContext(Dispatchers.IO) {
                try {

                    conversationId.validateConversationId()
                    Log.d(TAG, "markConversationAsRead: ${conversationId}")

                    val result = remote.markConversationAsRead(conversationId)

                    result.getOrElse { error ->
                        Log.e(TAG, "markConversationAsRead: ", error)
                        throw if (error is ErrorException) {
                            error
                        } else {
                            ErrorHandler.handleException(error)
                        }
                    }.also {
                        Log.d(TAG, "markConversationAsRead: successfully")
                    }
                } catch (e: ErrorException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "markConversationAsRead: ", e)
                    throw ErrorHandler.handleException(e)
                }
            }
        }
    }

    override suspend fun createOrGetConversation(
        buyerId: String,
        sellerId: String
    ): Result<String> {
        return safeApiCall {
            withContext(Dispatchers.IO) {
                try {
                    buyerId.validateUserId()
                    sellerId.validateUserId()

                    // Validate not same user
                    if (buyerId == sellerId) {
                        throw ErrorException.ValidationException.InvalidUserId()
                    }

                    Log.d(TAG, "Creating/Getting conversation between $buyerId and $sellerId")

                    // Create/Get via remote
                    val result = remote.createOrGetConversation(
                        buyerId = buyerId,
                        sellerId = sellerId
                    )

                    // Handle result
                    result.getOrElse { error ->
                        Log.e(TAG, "Error from remote createOrGetConversation", error)
                        throw error as? ErrorException ?: ErrorHandler.handleException(error)
                    }.also { conversationId ->
                        Log.d(TAG, "Conversation created/retrieved: $conversationId")
                    }
                } catch (e: ErrorException) {
                    throw e
                } catch (e: Exception) {
                    throw ErrorHandler.handleException(e)
                }
            }
        }
    }

    override suspend fun subscribeToMessages(
        conversationId: String,
        onNewMessage: (Message) -> Unit
    ): RealtimeChannel {
        return try {
            conversationId.validateConversationId()

            remote.subscribeToMessages(
                conversationId = conversationId,
                onNewMessage = { messageResponse ->
                    try {
                        val message = messageResponse.toEntity()
                        Log.d(TAG, "Received new message: ${message.id}")
                        onNewMessage(message)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing new message", e)
                    }
                }
            ).also {
                Log.d(TAG, "Successfully subscribed to messages")
            }
        } catch (e: ErrorException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to messages", e)
            throw ErrorException.RealtimeException.SubscriptionFailed()
        }
    }

}