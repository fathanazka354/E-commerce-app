package com.fathan.e_commerce.domain.usecase.chats

import android.net.Uri
import com.fathan.e_commerce.domain.repository.ChatRepository
import javax.inject.Inject

class SendAudioMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(audioUri: Uri, durationText: String) =
        repository.sendAudio(audioUri, durationText)
}