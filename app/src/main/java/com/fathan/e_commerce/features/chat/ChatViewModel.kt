package com.fathan.e_commerce.features.chat
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.data.local.Message
import com.fathan.e_commerce.domain.usecase.chats.ChatUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val displayedMessages: List<Message> = emptyList(), // Data yang ditampilkan (Hasil Filter/Search)
    val activeFilter: ChatFilter = ChatFilter.ALL,
)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val useCases: ChatUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState(isLoading = true))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        refreshMessages()
    }

    private fun refreshMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val data = useCases.getMessages()
                _uiState.update {
                    it.copy(
                        messages = data,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun sendText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            useCases.sendText(text)
            refreshMessages()
        }
    }

    fun sendImage(imageUri: Uri) {
        viewModelScope.launch {
            useCases.sendImage(imageUri)
            refreshMessages()
        }
    }

    fun sendAudio(audioUri: Uri, durationMs: Long) {
        val durationText = formatDuration(durationMs)
        viewModelScope.launch {
            useCases.sendAudio(audioUri, durationText)
            refreshMessages()
        }
    }

    // --- Logic Filtering (Semua / Belum Dibaca / Sudah Dibaca) ---
    fun updateFilter(filter: ChatFilter) {
        _uiState.update { state ->
            val filteredList = filterList(state.messages, filter)
            state.copy(activeFilter = filter, displayedMessages = filteredList)
        }
    }

    // --- Logic Pencarian ---
    fun searchChat(query: String) {
        _uiState.update { state ->
            // 1. Ambil list berdasarkan filter yang sedang aktif dulu
            val baseList = filterList(state.messages, state.activeFilter)

            // 2. Lalu filter berdasarkan query pencarian
            val searchedList = if (query.isBlank()) {
                baseList
            } else {
                baseList.filter {
                    it.name?.contains(query, ignoreCase = true)?: false ||
                            it.message?.contains(query, ignoreCase = true)?: false
                }
            }
            state.copy(displayedMessages = searchedList)
        }
    }

    fun markAllAsRead() {
        _uiState.update { state ->
            val updatedAll = state.messages.map { it.copy(unreadCount = 0) }

            val updatedDisplayed = filterList(updatedAll, state.activeFilter)

            state.copy(messages = updatedAll, displayedMessages = updatedDisplayed)
        }
    }

    fun deleteChat(id: Int) {
        _uiState.update { state ->
            val updatedAll = state.messages.filterNot { it.id == id }

            val updatedDisplayed = filterList(updatedAll, state.activeFilter)

            state.copy(messages = updatedAll, displayedMessages = updatedDisplayed)
        }
    }

    private fun filterList(list: List<Message>, filter: ChatFilter): List<Message> {
        return when (filter) {
            ChatFilter.ALL -> list
            ChatFilter.UNREAD -> list.filter { it.unreadCount > 0 }
            ChatFilter.READ -> list.filter { it.unreadCount == 0 }
        }
    }
}

enum class ChatFilter(val label: String) {
    ALL("Semua"),
    UNREAD("Belum Dibaca"),
    READ("Sudah Dibaca")
}