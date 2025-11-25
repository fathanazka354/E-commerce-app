package com.fathan.e_commerce.domain.usecase.chats

import android.net.Uri
import com.fathan.e_commerce.domain.repository.ChatRepository

class SendImageMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(imageUri: Uri) = repository.sendImage(imageUri)
}