package com.fathan.e_commerce.domain.usecase.chats
import android.net.Uri
import com.fathan.e_commerce.data.local.Message
import com.fathan.e_commerce.domain.repository.ChatRepository

class GetMessagesUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): List<Message> = repository.getMessages()
}


