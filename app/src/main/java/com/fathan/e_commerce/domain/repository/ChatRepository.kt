//package com.fathan.e_commerce.domain.repository
//
//import android.net.Uri
//import com.fathan.e_commerce.features.chat.domain.entity.Message
//import kotlinx.coroutines.flow.Flow
//
//interface ChatRepository {
//    fun subscribe(roomId: String): Flow<Message>
//
//    suspend fun loadHistory(roomId: String): List<Message>
//
//    suspend fun sendText(roomId: String, senderId: String, text: String)
//
//    suspend fun sendImage(roomId: String, senderId: String, bytes: ByteArray, ext: String, mime: String)
//}