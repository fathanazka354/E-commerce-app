package com.fathan.e_commerce.ui.chat
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.data.local.Message
import com.fathan.e_commerce.domain.usecase.chats.ChatUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel(
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
}
@RequiresApi(Build.VERSION_CODES.O)
class ChatViewModelFactory (
    private val useCases: ChatUseCases = ChatUseCases.provideDefault()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(useCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}