package com.fathan.e_commerce.features.chat.ui

import android.app.Application
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.features.chat.domain.entity.ChatMessage
import com.fathan.e_commerce.features.chat.domain.entity.ConversationItem
import com.fathan.e_commerce.features.chat.domain.entity.MessageType
import com.fathan.e_commerce.features.chat.domain.usecase.ChatUseCases
import com.fathan.e_commerce.features.chat.utils.ChatFilter
import com.fathan.e_commerce.features.components.AudioPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val conversations: List<ConversationItem> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val displayedMessages: List<ConversationItem> = emptyList(),
    val activeFilter: ChatFilter = ChatFilter.ALL,
    val uploadingMedia: Map<String, Float> = emptyMap()
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val useCases: ChatUseCases,
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatUiState(isLoading = true))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Audio player
    private val audioPlayer = AudioPlayerManager()
    val audioPlaybackState = audioPlayer.playbackState

    private var currentRoomId: String? = null
    private var myAuthId: String? = null

    init {
        viewModelScope.launch {
            loadConversations()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }

    fun openRoom(roomId: String, myUserId: String) {
        currentRoomId = roomId
        myAuthId = myUserId

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    messages = emptyList(),
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                // ✅ PERBAIKAN: Pisahkan pemanggilan dan tangkap hasil dengan benar
                useCases.readByRoom(roomId)
                val messages = useCases.fetchChatByRoomWithStatus(roomId)
                messages // Return messages explicitly
            }.onSuccess { messages ->
                Log.e("CHAT_DEBUG", "ROOM=$roomId, fetched=${messages.size}")
                messages.forEach {
                    Log.e("CHAT_DEBUG", "msg: ${it.id} from ${it.senderId}, isMe=${it.senderId == myAuthId}")
                }

                // ✅ PERBAIKAN: Set isMe flag dengan benar
                val enrichedMessages = messages.map { msg ->
                    msg.copy(isMe = msg.senderId == myAuthId)
                }

                // ✅ CRITICAL FIX: Update state dengan messages DAN set isLoading = false
                _uiState.update {
                    it.copy(
                        messages = enrichedMessages,
                        isLoading = false,
                        errorMessage = null
                    )
                }

                // Subscribe setelah state ter-update
                subscribeRealtime()

            }.onFailure { e ->
                Log.e("CHAT_DEBUG", "ROOM=$roomId, error=${e.message}")
                e.printStackTrace()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun sendText(text: String) {
        val roomId = currentRoomId ?: return
        val userId = myAuthId ?: return
        if (text.isBlank()) return

        val optimistic = ChatMessage(
            id = "local-${UUID.randomUUID()}",
            roomId = roomId,
            senderId = userId,
            message = text,
            type = MessageType.TEXT,
            createdAt = ZonedDateTime.now().toString(),
            isMe = true
        )

        _uiState.update {
            it.copy(messages = it.messages + optimistic)
        }

        viewModelScope.launch {
            runCatching {
                useCases.sendText(roomId, userId, text)
            }.onFailure {
                _uiState.update {
                    it.copy(errorMessage = "Send failed")
                }
            }
        }
    }

    fun sendImage(uri: Uri) {
        val roomId = currentRoomId ?: return
        val userId = myAuthId ?: return

        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                application.contentResolver.openInputStream(uri)?.readBytes()
            } ?: return@launch

            val mime = application.contentResolver.getType(uri) ?: "image/jpeg"

            val localId = "local-image-${UUID.randomUUID()}"
            val optimisticMessage = ChatMessage(
                id = localId,
                roomId = roomId,
                senderId = userId,
                type = MessageType.IMAGE,
                message = "",
                mediaUrl = uri.toString(),
                createdAt = ZonedDateTime.now().toString(),
                isMe = true
            )

            _uiState.update {
                it.copy(messages = it.messages + optimisticMessage)
            }

            runCatching {
                val serverMessage = useCases.sendImage(
                    roomId = roomId,
                    bytes = bytes,
                    mime = mime,
                    filename = "image_${System.currentTimeMillis()}.jpg",
                    caption = null,
                    senderId = userId
                )
                serverMessage
            }.onSuccess { serverMessage ->
                Log.d("ChatViewModel", "Image uploaded successfully: ${serverMessage?.id}")

                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.map { msg ->
                            if (msg.id == localId && serverMessage != null) {
                                serverMessage.copy(isMe = serverMessage.senderId == myAuthId)
                            } else {
                                msg
                            }
                        }
                    )
                }
            }.onFailure { e ->
                Log.e("ChatViewModel", "Image upload failed", e)
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.map {
                            if (it.id == localId) {
                                it.copy(metadata = mapOf("upload_failed" to "true"))
                            } else it
                        },
                        errorMessage = "Image upload failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun sendAudioFile(file: File, durationMs: Long) {
        val roomId = currentRoomId ?: return
        val userId = myAuthId ?: return

        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                file.readBytes()
            }

            val mime = "audio/mp4"

            val localId = "local-audio-${UUID.randomUUID()}"
            val optimisticMessage = ChatMessage(
                id = localId,
                roomId = roomId,
                senderId = userId,
                message = "",
                type = MessageType.AUDIO,
                mediaUrl = null,
                metadata = mapOf(
                    "uploading" to "true",
                    "duration_ms" to durationMs.toString()
                ),
                isRead = false,
                createdAt = ZonedDateTime.now().toString(),
                isMe = true
            )

            _uiState.update {
                it.copy(messages = it.messages + optimisticMessage)
            }

            runCatching {
                val serverMessage = useCases.sendAudio(
                    roomId = roomId,
                    bytes = bytes,
                    mime = mime,
                    filename = "audio_${System.currentTimeMillis()}.m4a",
                    durationMs = durationMs
                )
                serverMessage
            }.onSuccess { serverMessage ->
                Log.d("ChatViewModel", "Audio uploaded: ${serverMessage?.id}")

                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.map { msg ->
                            if (msg.id == localId && serverMessage != null) {
                                serverMessage.copy(isMe = serverMessage.senderId == myAuthId)
                            } else {
                                msg
                            }
                        }
                    )
                }

                file.delete()
            }.onFailure { e ->
                Log.e("ChatViewModel", "Audio upload failed", e)
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.map {
                            if (it.id == localId) {
                                it.copy(metadata = mapOf("upload_failed" to "true"))
                            } else it
                        },
                        errorMessage = "Audio upload failed: ${e.message}"
                    )
                }
                file.delete()
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                myAuthId?.let { useCases.markAllAsRead(it) }
                _uiState.update { state ->
                    val convs = state.conversations.map { it.copy(unreadCount = 0) }
                    state.copy(conversations = convs, displayedMessages = convs)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Mark all read failed") }
            }
        }
    }

    fun findChat(query: String) {
        viewModelScope.launch {
            try {
                val results = useCases.findChat(query, null)
                val mapped = results.map {
                    ConversationItem(
                        lastMessageTime = it.createdAt,
                        unreadCount = 0,
                        lastSenderId = it.senderId,
                        roomId = it.roomId,
                        lastMessage = it.message,
                        isMe = it.isMe,
                        otherUserName = it.otherUserName,
                        otherUserAvatar = it.otherUserAvatar,
                        otherUserEmail = it.otherUserEmail,
                        isRead = it.isRead,
                        messageType = it.type
                    )
                }
                _uiState.update { it.copy(displayedMessages = mapped) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Search failed") }
            }
        }
    }

    fun updateFilter(filter: ChatFilter) {
        _uiState.update { state ->
            val filtered = when (filter) {
                ChatFilter.ALL -> state.conversations
                ChatFilter.UNREAD -> state.conversations.filter { it.unreadCount > 0 }
                ChatFilter.READ -> state.conversations.filter { it.unreadCount == 0 }
            }
            state.copy(activeFilter = filter, displayedMessages = filtered)
        }
    }

    private suspend fun loadConversations() {
        _uiState.update { it.copy(isLoading = true) }

        runCatching {
            useCases.fetchAllChats()
        }.onSuccess { list ->
            Log.d("ChatViewModel", "loadConversations: ${list.size}")
            _uiState.update {
                it.copy(
                    conversations = list,
                    displayedMessages = list,
                    isLoading = false
                )
            }
        }.onFailure { e ->
            Log.e("ChatViewModel", "loadConversations: ${e.message}")

            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun subscribeRealtime() {
        viewModelScope.launch {
            useCases.incomingMessages.collect { incoming ->
                val roomId = currentRoomId ?: return@collect
                if (incoming.roomId != roomId) return@collect

                val enriched = incoming.copy(
                    isMe = incoming.senderId == myAuthId
                )

                _uiState.update { state ->
                    // ✅ Cek duplikasi sebelum menambahkan
                    if (state.messages.any { it.id == enriched.id }) return@update state

                    // ✅ Tambahkan message baru tanpa mengubah isLoading
                    state.copy(messages = state.messages + enriched)
                }
            }
        }
    }

    // ✅ Audio player controls
    fun playAudio(audioUrl: String?, messageId: String) {
        if (audioUrl != null) {
            audioPlayer.playAudio(audioUrl, messageId)
        }
    }

    fun pauseAudio() {
        audioPlayer.pause()
    }

    fun cyclePlaybackSpeed() {
        audioPlayer.cyclePlaybackSpeed()
    }

    fun seekAudio(position: Float) {
        audioPlayer.seekTo(position.toInt())
    }
}