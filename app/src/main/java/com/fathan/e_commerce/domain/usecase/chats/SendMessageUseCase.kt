package com.fathan.e_commerce.domain.usecase.chats

import com.fathan.e_commerce.domain.repository.ChatRepository

class SendTextMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(text: String) = repository.sendText(text)
}
