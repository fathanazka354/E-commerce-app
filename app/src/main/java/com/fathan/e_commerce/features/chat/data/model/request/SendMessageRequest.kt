package com.fathan.e_commerce.features.chat.data.model.request

import kotlinx.serialization.SerialName

data class SendMessageRequest(
    @SerialName("p_conversation_id")
    val conversationId: String,

    @SerialName("p_sender_id")
    val senderId: String,

    @SerialName("p_message_type")
    val messageType: String,

    @SerialName("p_message_content")
    val messageContent: String,

    @SerialName("p_product_id")
    val productId: Long? = null
)