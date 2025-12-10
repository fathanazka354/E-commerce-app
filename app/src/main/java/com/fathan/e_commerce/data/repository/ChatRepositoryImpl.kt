//package com.fathan.e_commerce.data.repository
//
//import android.net.Uri
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.ui.graphics.Color
//import com.fathan.e_commerce.data.local.Message
//import com.fathan.e_commerce.data.local.MessageType
//import com.fathan.e_commerce.domain.repository.ChatRepository
//import java.time.LocalDate
//import java.time.LocalTime
//import java.time.format.DateTimeFormatter
//
///**
// * Simple in-memory repo, cocok buat UI demo.
// */
//@RequiresApi(Build.VERSION_CODES.O)
//class ChatRepositoryImpl : ChatRepository {
//
//    private val messages = mutableListOf<Message>()
//    private var idCounter = 0
//
//    init {
//        // initial dummy data yang sebelumnya ada di ChatDetailScreen
//        val today = LocalDate.now()
//
//        messages += Message(
//            id = nextId(),
//            text = "Hey team, let's start the meeting.",
//            senderName = "John Doe",
//            time = "09:00",
//            date = today.minusDays(3),
//            isMe = false,
//            initial = "J",
//            message = "Hey you bro",
//            avatarColor = Color(0xFF8D6E63)
//        )
//        messages += Message(
//            id = nextId(),
//            text = "Here are the files.",
//            senderName = "Aliena",
//            time = "14:30",
//            isTyping = true,
//            date = today.minusDays(1),
//            isMe = false,
//            initial = "A",
//            message = "Whatsapp mamy",
//            avatarColor = Color(0xFFFFCCBC),
//            type = MessageType.IMAGE // imageUri nanti diisi saat attach
//        )
//        messages += Message(
//            id = nextId(),
//            text = "That was a good Idea Yefi",
//            senderName = "John Doe",
//            time = "09:18",
//            date = today,
//            isMe = false,
//
//            message = "Well...",
//            initial = "J",
//            avatarColor = Color(0xFF8D6E63)
//        )
//        messages += Message(
//            id = nextId(),
//            text = "But I think we should consider the budget...",
//            senderName = "John Doe",
//            time = "09:18",
//            date = today,
//            isMe = false,
//
//            message = "GG",
//            initial = "J",
//            avatarColor = Color(0xFF8D6E63),
//            reactions = listOf("🔥 1", "🔥 2")
//        )
//        messages += Message(
//            id = nextId(),
//            text = "I agree with this",
//            senderName = "Yefi",
//            time = "09:18",
//            date = today,
//            isMe = true,
//            initial = "Y",
//            message = "bro",
//            avatarColor = Color(0xFFBA68C8),
//            type = MessageType.REPLY,
//            replyToName = "John Doe",
//            replyText = "But I think we should consider the budget..."
//        )
//    }
//
//    private fun nextId(): Int = ++idCounter
//
//    private fun nowTime(): String =
//        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
//
//    private fun today(): LocalDate = LocalDate.now()
//
//    override suspend fun getMessages(): List<Message> =
//        messages.toList()
//
//    override suspend fun sendText(text: String) {
//        if (text.isBlank()) return
//        messages += Message(
//            id = nextId(),
//            text = text,
//            senderName = "Me",
//            time = nowTime(),
//            date = today(),
//            isMe = true,
//            initial = "M",
//            avatarColor = Color(0xFFE1BEE7),
//            type = MessageType.TEXT
//        )
//    }
//
//    override suspend fun sendImage(imageUri: Uri) {
//        messages += Message(
//            id = nextId(),
//            text = "",
//            senderName = "Me",
//            time = nowTime(),
//            date = today(),
//            isMe = true,
//            initial = "M",
//            avatarColor = Color(0xFFE1BEE7),
//            type = MessageType.IMAGE,
//            imageUri = imageUri
//        )
//    }
//
//    override suspend fun sendAudio(audioUri: Uri, durationText: String) {
//        messages += Message(
//            id = nextId(),
//            text = "",
//            senderName = "Me",
//            time = nowTime(),
//            date = today(),
//            isMe = true,
//            initial = "M",
//            avatarColor = Color(0xFFE1BEE7),
//            type = MessageType.AUDIO,
//            audioUri = audioUri,
//            name = "Milner",
//            audioDuration = durationText
//        )
//    }
//}