//package com.fathan.e_commerce.data.utils
//
//import android.util.Log
//import com.fathan.e_commerce.features.chat.domain.entity.ChatMessage
//import com.fathan.e_commerce.features.chat.domain.entity.ConversationItem
//import com.fathan.e_commerce.features.chat.domain.entity.MessageType
//import com.fathan.e_commerce.features.product.domain.entities.FlashSaleItem
//
//
//import com.fathan.e_commerce.features.product.data.model.CategoryDto
//import com.fathan.e_commerce.features.product.data.model.FlashSaleDto
//import com.fathan.e_commerce.features.product.data.model.ProductDto
//import com.fathan.e_commerce.features.product.domain.entities.Category
//import com.fathan.e_commerce.features.product.domain.entities.Product
//import kotlinx.datetime.Instant
//
//
//fun MessageResponse.toDomain(): ChatMessage {
//    return ChatMessage(
//        id = messageId?: id?:"",
//        roomId = roomId,
//        senderId = senderId,
//        message = content,
//        type = MessageType.valueOf(messageType.uppercase()),
//        mediaUrl = mediaUrl,
//        otherUserName = otherUserName,
//        isRead = isRead,
//        createdAt = Instant.parse(createdAt).toString(),
//    )
//}
//
//
//fun ConversationResponse.toDomain(userId: String): ConversationItem {
//    Log.d("ConversationResponse", "toDomain: ${otherUserName} || $otherUserEmail || ${lastMessage} || ${lastMessageTime} || ${isRead}")
//    return ConversationItem(
////        isGroup = isGroup,
////        title = title,
//        roomId = roomId,
//        lastMessage = lastMessage,
//        unreadCount = unreadCount,
//        lastSenderId = lastSenderId,
//        lastMessageTime = lastMessageTime,
//        isMe = lastSenderId == userId,
//        otherUserName = otherUserName,
//        otherUserAvatar = otherUserAvatar,
//        otherUserEmail = otherUserEmail,
//        isRead = isRead,
//        messageType = MessageType.from(lastMessageType)
//    )
//}
//
//fun CategoryDto.toDomain(): Category =
//    Category(id = id, name = name ?: "Unknown", iconEmoji = icon ?: "📦")
//
//fun ProductDto.toDomain(): Product =
//    Product(
//        id = id,
//        name = name,
//        price = price ?: 0.0,
//        storeId = store_id?.toInt() ?: 0,
//        sellerId = seller_id?.toInt() ?: 0,
//        rating = (rate ?: 0.0).toFloat(),
//        thumbnail = thumbnail,
//        description = description,
//        sold = sold
//    )
//
//fun FlashSaleDto.toDomain(product: Product? = null): FlashSaleItem =
//    FlashSaleItem(
//        id = id.toInt(),
//        productId = product?.id ?: product_id.toInt(),
//        flashPrice = flash_price ?: 0.0,
//        originalPrice = original_price ?: 0.0,
//        stock = flash_stock ?: 0L,
//        sold = (sold_qty ?: 0.0).toInt()
//    )
