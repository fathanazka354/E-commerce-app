package com.fathan.e_commerce.features.chat.ui.detail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.domain.model.AuthUser
import com.fathan.e_commerce.domain.usecase.auth.GetCurrentUserUseCase
import com.fathan.e_commerce.features.chat.domain.entity.Message
import com.fathan.e_commerce.features.chat.domain.usecase.ChatUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatUseCases: ChatUseCases,
    private val getCurrentUser: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"]) {
        "conversationId is required"
    }

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isUploadingMedia = MutableStateFlow(false)
    val isUploadingMedia: StateFlow<Boolean> = _isUploadingMedia.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null

    // ✅ Track processed message IDs to prevent duplicates
    private val processedMessageIds = mutableSetOf<String>()

    // ✅ Use atomic flag to prevent double subscription
    @Volatile
    private var isSubscribing = false

    init {
        Log.d(TAG, "🔵 ViewModel INIT for conversation: $conversationId")
        loadCurrentUser()
        loadMessages()
        subscribeToNewMessages()
        markAsRead()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = getCurrentUser()
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            Log.d(TAG, "📥 Loading messages for conversation: $conversationId")
            chatUseCases.fetchMessages(conversationId)
                .onSuccess { messages ->
                    _messages.value = messages
                    // Track existing message IDs
                    processedMessageIds.clear()
                    processedMessageIds.addAll(messages.map { it.id })
                    Log.d(TAG, "✅ Loaded ${messages.size} messages")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Failed to load messages", error)
                }
        }
    }

    // ✅ BALANCED: Works on first visit AND after back navigation
    private fun subscribeToNewMessages() {
        // ✅ Prevent concurrent subscription attempts
        if (isSubscribing) {
            Log.w(TAG, "⚠️ Already subscribing, skipping duplicate attempt")
            return
        }

        viewModelScope.launch {
            try {
                isSubscribing = true

                // ✅ Check if already have active channel
                if (realtimeChannel != null) {
                    Log.d(TAG, "🔄 Existing channel found, unsubscribing first...")
                    try {
                        realtimeChannel?.unsubscribe()
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ Error unsubscribing old channel: ${e.message}")
                    }
                    realtimeChannel = null
                    // Small delay to ensure cleanup completes
                    delay(300)
                }

                Log.d(TAG, "🔌 Creating new realtime subscription for: $conversationId")

                realtimeChannel = chatUseCases.subscribe(conversationId) { newMessage ->
                    Log.d(TAG, "📨 Realtime message received: ${newMessage.id}, type: ${newMessage.messageType}")

                    // ✅ Check if already processed
                    if (newMessage.id in processedMessageIds) {
                        Log.d(TAG, "⏭️ Message already processed, skipping: ${newMessage.id}")
                        return@subscribe
                    }

                    // ✅ Check if message already exists in list
                    if (_messages.value.none { it.id == newMessage.id }) {
                        processedMessageIds.add(newMessage.id)
                        _messages.value = _messages.value + newMessage
                        Log.d(TAG, "✅ Message added to list. Total: ${_messages.value.size}")
                        markAsRead()
                    } else {
                        Log.d(TAG, "⏭️ Message already in list, skipping duplicate")
                    }
                }

                Log.d(TAG, "✅ Successfully subscribed to realtime")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to subscribe to realtime", e)

                // ✅ Retry with exponential backoff
                delay(2000)
                Log.d(TAG, "🔄 Retrying subscription...")
                isSubscribing = false
                subscribeToNewMessages()
            } finally {
                isSubscribing = false
            }
        }
    }

    fun sendMessage(
        messageContent: String,
        messageType: String = "text",
        productId: Long? = null
    ) {
        viewModelScope.launch {
            _isSending.value = true

            Log.d(TAG, "📤 Sending text message: '$messageContent'")

            // ✅ OPTIMISTIC UPDATE
            val tempId = "temp_${System.currentTimeMillis()}"
            val tempMessage = Message(
                id = tempId,
                conversationId = conversationId,
                senderId = _currentUser.value?.uid ?: "",
                messageType = messageType,
                messageContent = messageContent,
                productId = productId,
                isRead = false,
                readAt = null,
                createdAt = java.time.Instant.now().toString()
            )

            _messages.value = _messages.value + tempMessage
            processedMessageIds.add(tempId)

            chatUseCases.sendText(
                conversationId = conversationId,
                messageType = messageType,
                messageContent = messageContent,
                productId = productId
            )
                .onSuccess { messageId ->
                    Log.d(TAG, "✅ Text message sent successfully: $messageId")
                    // Remove temp message
                    _messages.value = _messages.value.filter { it.id != tempId }
                    processedMessageIds.remove(tempId)
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Failed to send text message", error)
                    _messages.value = _messages.value.filter { it.id != tempId }
                    processedMessageIds.remove(tempId)
                }

            _isSending.value = false
        }
    }

    // ✅ CAREFUL: Only refresh if needed (called from lifecycle)
    fun refreshMessages() {
        Log.d(TAG, "🔄 refreshMessages() called")

        // Reload messages from server
        loadMessages()

        // ✅ Only resubscribe if channel is null or dead
        if (realtimeChannel == null) {
            Log.d(TAG, "🔌 No active channel, resubscribing...")
            subscribeToNewMessages()
        } else {
            Log.d(TAG, "✅ Active channel exists, skipping resubscription")
        }
    }

    fun sendImage(imageUri: Uri) {
        viewModelScope.launch {
            _isUploadingMedia.value = true
            Log.d(TAG, "📤 Sending image: $imageUri")

            val tempId = "temp_image_${System.currentTimeMillis()}"
            val placeholderMessage = Message(
                id = tempId,
                conversationId = conversationId,
                senderId = _currentUser.value?.uid ?: "",
                messageType = "image",
                messageContent = "{\"url\":\"uploading\",\"width\":0,\"height\":0}",
                productId = null,
                isRead = false,
                readAt = null,
                createdAt = java.time.Instant.now().toString()
            )

            _messages.value = _messages.value + placeholderMessage
            processedMessageIds.add(tempId)

            chatUseCases.sendImage(
                conversationId = conversationId,
                imageUri = imageUri
            )
                .onSuccess { messageId ->
                    Log.d(TAG, "✅ Image sent successfully: $messageId")
                    _messages.value = _messages.value.filter { it.id != tempId }
                    processedMessageIds.remove(tempId)
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Failed to send image", error)
                    _messages.value = _messages.value.filter { it.id != tempId }
                    processedMessageIds.remove(tempId)
                }

            _isUploadingMedia.value = false
        }
    }

    fun sendAudio(audioUri: Uri, duration: Long) {
        viewModelScope.launch {
            _isUploadingMedia.value = true
            Log.d(TAG, "📤 Sending audio: $audioUri, duration: $duration")

            val tempId = "temp_audio_${System.currentTimeMillis()}"
            val placeholderMessage = Message(
                id = tempId,
                conversationId = conversationId,
                senderId = _currentUser.value?.uid ?: "",
                messageType = "audio",
                messageContent = "{\"url\":\"uploading\",\"duration\":$duration}",
                productId = null,
                isRead = false,
                readAt = null,
                createdAt = java.time.Instant.now().toString()
            )

            _messages.value = _messages.value + placeholderMessage
            processedMessageIds.add(tempId)

            chatUseCases.sendAudio(
                conversationId = conversationId,
                audioUri = audioUri,
                duration = duration
            )
                .onSuccess { messageId ->
                    Log.d(TAG, "✅ Audio sent successfully: $messageId")
                    _messages.value = _messages.value.filter { it.id != tempId }
                    processedMessageIds.remove(tempId)
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Failed to send audio", error)
                    _messages.value = _messages.value.filter { it.id != tempId }
                    processedMessageIds.remove(tempId)
                }

            _isUploadingMedia.value = false
        }
    }

    fun markAsRead() {
        viewModelScope.launch {
            chatUseCases.markAsRead(conversationId)
                .onFailure { error ->
                    Log.e(TAG, "❌ Failed to mark as read", error)
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🔴 ViewModel CLEARING, unsubscribing from realtime...")
        viewModelScope.launch {
            try {
                realtimeChannel?.let { channel ->
                    channel.unsubscribe()
                    Log.d(TAG, "✅ Successfully unsubscribed from realtime channel")
                }
                realtimeChannel = null
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error unsubscribing from channel", e)
            }
        }
    }

    companion object {
        private const val TAG = "ChatDetailViewModel"
    }
}