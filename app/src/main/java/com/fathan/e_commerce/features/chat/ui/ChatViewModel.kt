package com.fathan.e_commerce.features.chat.ui

import android.app.Application
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.features.chat.domain.entity.ChatMessage
import com.fathan.e_commerce.features.chat.domain.entity.MessageType
import com.fathan.e_commerce.features.chat.domain.usecase.ChatUseCases
import com.fathan.e_commerce.features.chat.utils.ChatFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

data class ChatUiState(
    val conversations: List<ChatMessage> = emptyList(),      // latest-per-room list for ChatScreen
    val messages: List<ChatMessage> = emptyList(),           // current room messages for ChatDetailScreen
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val displayedMessages: List<ChatMessage> = emptyList(),  // for ChatScreen's search/filter
    val activeFilter: ChatFilter = ChatFilter.ALL,
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val useCases: ChatUseCases,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatUiState(isLoading = true))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // current context
    private var currentRoomId: String? = null
    private var myAuthId: String? = null

    init {
        // load conversation list then subscribe realtime
        viewModelScope.launch {
            loadConversations()
            subscribeRealtime()
        }
    }

    // -----------------------------------------------------------
    // Public API for UI
    // -----------------------------------------------------------
    /**
     * Set current room & current user, then load room messages.
     * Call this when opening ChatDetailScreen.
     */
    fun setRoom(roomId: String, myAuthUuid: String) {
        currentRoomId = roomId
        myAuthId = myAuthUuid
        viewModelScope.launch {
            loadRoomMessages(roomId)
            // When opening a room, mark as read (server-side) and update local state
            try {
                useCases.readByRoom(roomId)
                markRoomReadLocal(roomId)
            } catch (e: Exception) {
                // ignore or handle
            }
        }
    }

    fun sendText(text: String) {
        if (text.isBlank() || currentRoomId == null || myAuthId == null) return
        viewModelScope.launch {
            try {
                useCases.sendText(currentRoomId!!, myAuthId!!, text)
                // rely on realtime to append; fallback refresh if desired
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Send failed") }
            }
        }
    }

    fun sendImage(imageUri: Uri) {
        if (currentRoomId == null || myAuthId == null) return
        viewModelScope.launch {
            try {
                val bytes = readBytes(imageUri)
                val mime = getMimeType(imageUri) ?: "image/jpeg"
                val ext = mime.substringAfterLast("/").takeIf { it.isNotBlank() } ?: "jpg"
                val filename = "img_${System.currentTimeMillis()}.$ext"
                useCases.sendImage(currentRoomId!!, myAuthId!!, bytes, mime, filename, null)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Image send failed") }
            }
        }
    }

    fun sendAudio(audioUri: Uri, durationMs: Long) {
        if (currentRoomId == null || myAuthId == null) return
        viewModelScope.launch {
            try {
                val bytes = readBytes(audioUri)
                val mime = getMimeType(audioUri) ?: "audio/m4a"
                val ext = mime.substringAfterLast("/").takeIf { it.isNotBlank() } ?: "m4a"
                val filename = "audio_${System.currentTimeMillis()}.$ext"
                useCases.sendAudio(currentRoomId!!, myAuthId!!, bytes, mime, filename, durationMs)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Audio send failed") }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                // require myAuthId to call backend
                myAuthId?.let { useCases.markAllAsRead(it) }
                // local update
                _uiState.update { state ->
                    val convs = state.conversations.map { it.copy(unreadCount = 0) }
                    val msgs = state.messages.map { it.copy(unreadCount = 0) }
                    state.copy(conversations = convs, messages = msgs, displayedMessages = convs)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Mark all read failed") }
            }
        }
    }

    fun deleteChat(messageId: String) {
        viewModelScope.launch {
            try {
                useCases.deleteChat(messageId)
                // local remove
                _uiState.update { state ->
                    val convs = state.conversations.filterNot { it.id == messageId }
                    val msgs = state.messages.filterNot { it.id == messageId }
                    state.copy(conversations = convs, messages = msgs, displayedMessages = convs)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Delete failed") }
            }
        }
    }

    fun findChat(query: String) {
        viewModelScope.launch {
            try {
                val results = useCases.findChat(query, null)
                val mapped = results.map { domainToUi(it) }
                // show results in displayedMessages (conversation list view)
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

    // -----------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------
    private suspend fun loadConversations() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val list = useCases.fetchAllChats()
            // convert to latest-per-room + unread count per room
            val grouped = list.groupBy { it.roomId }
            val conv = grouped.mapNotNull { (_, msgs) ->
                val latest = msgs.maxByOrNull { parseCreatedAt(it.createdAt) } ?: return@mapNotNull null
                val unread = msgs.count { !it.isRead && it.senderId != myAuthId } // unread for me
                domainToUi(latest).copy(unreadCount = unread)
            }.sortedByDescending { parseCreatedAt(it.createdAt) }
            _uiState.update { it.copy(conversations = conv, displayedMessages = conv, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Load conv failed") }
        }
    }

    private suspend fun loadRoomMessages(roomId: String) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            Log.d("ChatViewModel", "loadRoomMessages: ${roomId}")
            val list = useCases.fetchChatByRoom(roomId)
            val mapped = list.map { domainToUi(it) }.sortedBy { parseCreatedAt(it.createdAt) } // ascending by createdAt
            _uiState.update { it.copy(messages = mapped, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Load messages failed") }
        }
    }

    private fun markRoomReadLocal(roomId: String) {
        _uiState.update { state ->
            val convs = state.conversations.map { if (it.roomId == roomId) it.copy(unreadCount = 0) else it }
            val msgs = state.messages.map { it.copy(unreadCount = 0) }
            state.copy(conversations = convs, messages = msgs, displayedMessages = convs)
        }
    }

    // subscribe to realtime and merge incoming messages
    @RequiresApi(Build.VERSION_CODES.O)
    private fun subscribeRealtime() {
        viewModelScope.launch {
            try {
                useCases.incomingMessages.collect { incomingRaw ->
                    val incoming = domainToUi(incomingRaw)

                    _uiState.update { state ->
                        // avoid duplicate message ids
                        val alreadyExistsInMessages = state.messages.any { it.id == incoming.id }
                        val newMessages = if (incoming.roomId == currentRoomId && !alreadyExistsInMessages) {
                            state.messages + incoming
                        } else state.messages

                        // update conversations map:
                        val convsMap = state.conversations.associateBy { it.roomId }.toMutableMap()
                        val prev = convsMap[incoming.roomId]

                        val updatedLatest = if (prev == null) {
                            // first conversation entry
                            incoming.copy(unreadCount = if (incoming.senderId != myAuthId) 1 else 0)
                        } else {
                            // compare by createdAt
                            val incomingTime = parseCreatedAt(incoming.createdAt)
                            val prevTime = parseCreatedAt(prev.createdAt)
                            if (incomingTime.isAfter(prevTime)) {
                                incoming.copy(unreadCount = prev.unreadCount + if (incoming.senderId != myAuthId) 1 else 0)
                            } else prev
                        }

                        convsMap[incoming.roomId] = updatedLatest
                        val convList = convsMap.values.sortedByDescending { parseCreatedAt(it.createdAt) }

                        state.copy(conversations = convList, messages = newMessages, displayedMessages = convList)
                    }
                }
            } catch (_: Exception) {
                // ignore realtime errors
            }
        }
    }

    // Mapping: enrich raw ChatMessage -> ChatMessage (fills time/date/uri/isMe/initial/type etc)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun domainToUi(raw: ChatMessage): ChatMessage {
        val created = parseCreatedAt(raw.createdAt)
        val timeStr = toTimeString(created)
        val dateStr = toDateString(created)
        val isMe = raw.senderId == myAuthId

        val msgType = when ((raw.messageType ?: "text").lowercase(Locale.getDefault())) {
            "image" -> MessageType.IMAGE
            "audio" -> MessageType.AUDIO
            "reply" -> MessageType.REPLY
            else -> MessageType.TEXT
        }

        val initial = raw.senderName?.firstOrNull()?.uppercaseChar()?.toString()
            ?: raw.senderId?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

        return raw.copy(
            senderName = raw.senderName ?: raw.senderId,
            message = raw.content ?: raw.message,
            isMe = isMe,
            initial = initial,
            avatarColor = raw.avatarColor ?: Color(0xFFE1BEE7),
            isOnline = raw.isOnline,
            isTyping = raw.isTyping,
            time = timeStr,
            unreadCount = if (raw.isRead) 0 else (raw.unreadCount.takeIf { it > 0 } ?: 1),
            imageUri = raw.mediaUrl?.let { Uri.parse(it) } ?: raw.imageUri,
            audioUri = raw.mediaUrl?.let { Uri.parse(it) } ?: raw.audioUri,
            audioDuration = raw.metadata?.get("duration_ms") ?: raw.audioDuration,
            type = msgType,
            date = dateStr
        )
    }

    // helper to read bytes from Uri
    private suspend fun readBytes(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val cr = getApplication<Application>().contentResolver
        cr.openInputStream(uri)!!.use { it.readBytes() }
    }

    private fun getMimeType(uri: Uri): String? {
        val cr = getApplication<Application>().contentResolver
        return cr.getType(uri)
    }

    // date/time helpers
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseCreatedAt(createdAt: String?): ZonedDateTime {
        return try {
            if (createdAt == null) ZonedDateTime.now() else ZonedDateTime.parse(createdAt)
        } catch (e: Exception) {
            ZonedDateTime.now()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toTimeString(zdt: ZonedDateTime): String =
        zdt.format(DateTimeFormatter.ofPattern("HH:mm"))

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toDateString(zdt: ZonedDateTime): String =
        zdt.toLocalDate().toString()
}
