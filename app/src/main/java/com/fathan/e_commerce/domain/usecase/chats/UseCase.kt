package com.fathan.e_commerce.domain.usecase.chats

import android.os.Build
import androidx.annotation.RequiresApi
import com.fathan.e_commerce.data.repository.ChatRepositoryImpl

data class ChatUseCases(
    val getMessages: GetMessagesUseCase,
    val sendText: SendTextMessageUseCase,
    val sendImage: SendImageMessageUseCase,
    val sendAudio: SendAudioMessageUseCase
) {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun provideDefault(): ChatUseCases {
            val repo = ChatRepositoryImpl()
            return ChatUseCases(
                getMessages = GetMessagesUseCase(repo),
                sendText = SendTextMessageUseCase(repo),
                sendImage = SendImageMessageUseCase(repo),
                sendAudio = SendAudioMessageUseCase(repo)
            )
        }
    }
}