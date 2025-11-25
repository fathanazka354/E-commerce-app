package com.fathan.e_commerce.domain.repository

import android.net.Uri
import com.fathan.e_commerce.data.local.Message

interface ChatRepository {
    suspend fun getMessages(): List<Message>
    suspend fun sendText(text: String)
    suspend fun sendImage(imageUri: Uri)
    suspend fun sendAudio(audioUri: Uri, durationText: String)
}