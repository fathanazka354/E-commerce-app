package com.fathan.e_commerce.features.chat.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class MessageDetailResponse(
    @SerialName("message_id")
    val messageId: String,

    @SerialName("room_id")
    val roomId: String,

    @SerialName("sender_id")
    val senderId: String,

    val content: String?,
    val metadata: JsonElement?,

    @SerialName("message_type")
    val messageType: String,

    @SerialName("media_url")
    val mediaUrl: String?,

    @SerialName("created_at")
    val createdAt: String,

    // Room info
    @SerialName("product_id")
    val productId: Int?,

    @SerialName("room_created_at")
    val roomCreatedAt: String,

    @SerialName("room_buyer_id")
    val roomBuyerId: String,

    @SerialName("room_seller_id")
    val roomSellerId: String,

    // Product
    @SerialName("product_title")
    val productTitle: String?,

    @SerialName("product_price")
    val productPrice: Long?,

    @SerialName("product_image")
    val productImage: String?,

    // Sender
    @SerialName("sender_name")
    val senderName: String?,

    @SerialName("sender_avatar")
    val senderAvatar: String?,

    @SerialName("sender_email")
    val senderEmail: String?,

    // Seller
    @SerialName("seller_name")
    val sellerName: String?,

    @SerialName("seller_avatar")
    val sellerAvatar: String?,

    @SerialName("seller_email")
    val sellerEmail: String?,

    // Buyer
    @SerialName("buyer_name")
    val buyerName: String?,

    @SerialName("buyer_avatar")
    val buyerAvatar: String?,

    @SerialName("buyer_email")
    val buyerEmail: String?
) {
    // ✅ Computed properties di Kotlin
    fun getOtherUserName(currentUserId: String): String? {
        return if (roomBuyerId == currentUserId) sellerName else buyerName
    }

    fun getOtherUserAvatar(currentUserId: String): String? {
        return if (roomBuyerId == currentUserId) sellerAvatar else buyerAvatar
    }

    fun getOtherUserEmail(currentUserId: String): String? {
        return if (roomBuyerId == currentUserId) sellerEmail else buyerEmail
    }

    fun getOtherUserId(currentUserId: String): String {
        return if (roomBuyerId == currentUserId) roomSellerId else roomBuyerId
    }
}

@Serializable
data class MessageReadRecord(
    @SerialName("message_id")
    val messageId: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("read_at")
    val readAt: String
)

@Serializable
data class MessageWithReadStatus(
    @SerialName("message_id")
    val messageId: String,

    @SerialName("room_id")
    val roomId: String,

    @SerialName("sender_id")
    val senderId: String,

    val content: String?,
    val metadata: JsonElement?,

    @SerialName("message_type")
    val messageType: String,

    @SerialName("media_url")
    val mediaUrl: String?,

    @SerialName("created_at")
    val createdAt: String,

    // Product
    @SerialName("product_id")
    val productId: Int?,

    @SerialName("product_title")
    val productTitle: String?,

    @SerialName("product_price")
    val productPrice: Long?,

    @SerialName("product_image")
    val productImage: String?,

    // Names
    @SerialName("sender_name")
    val senderName: String?,

    @SerialName("buyer_name")
    val buyerName: String?,

    @SerialName("seller_name")
    val sellerName: String?,

    // Room
    @SerialName("room_buyer_id")
    val roomBuyerId: String,

    @SerialName("room_seller_id")
    val roomSellerId: String,

    // Read status (🔥 from SQL)
    @SerialName("is_read")
    val isRead: Boolean,

    @SerialName("read_at")
    val readAt: String?
)


fun JsonElement.toStringMap(): Map<String, String>? {
    return this.jsonObject.mapValues { it.value.jsonPrimitive.content }
}
