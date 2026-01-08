package com.fathan.e_commerce.features.chat.domain.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MessageTypeSerializer::class)
enum class MessageType {
    TEXT, IMAGE, AUDIO, REPLY;

    companion object {
        fun from(raw: String?): MessageType =
            when (raw?.lowercase()) {
                "image" -> IMAGE
                "audio" -> AUDIO
                "reply" -> REPLY
                else -> TEXT
            }
    }
}

// ✅ Custom Serializer
object MessageTypeSerializer : KSerializer<MessageType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("MessageType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MessageType) {
        encoder.encodeString(value.name.lowercase())
    }

    override fun deserialize(decoder: Decoder): MessageType {
        val string = decoder.decodeString()
        return MessageType.from(string)
    }
}
// Message.kt
data class Message(
    val id: String,

    val conversationId: String,

    val senderId: String,

    val messageType: String, // "text", "image", "product_card", "system"

    val messageContent: String,

    val productId: Long?,

    val isRead: Boolean,

    val readAt: String?,

    val createdAt: String,

    val updatedAt: String? = "",
    val isCurrentUser: Boolean? = true
)