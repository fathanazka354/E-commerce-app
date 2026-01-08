package com.fathan.e_commerce.features.chat.ui.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.features.chat.domain.entity.ConversationItem
import com.fathan.e_commerce.features.chat.domain.usecase.ChatUseCases
import com.fathan.e_commerce.features.chat.domain.usecase.FetchAllChats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatUseCases: ChatUseCases
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ConversationItem>>(emptyList())
    val conversations: StateFlow<List<ConversationItem>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadConversations()
    }

    // ✅ Add public refresh function
    fun refreshConversations() {
        Log.d(TAG, "Refreshing conversations...")
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true

                chatUseCases.fetchAllChats()
                .onSuccess { conversations ->
                    _conversations.value = conversations
                }
                .onFailure { error ->
                    // Handle error
                    Log.e("ChatListVM", "Failed to load conversations", error)
                }

            _isLoading.value = false
        }
    }

    companion object {
        private const val TAG = "ChatListViewModel"
    }
}