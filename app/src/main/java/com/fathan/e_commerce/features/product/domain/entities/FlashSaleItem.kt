package com.fathan.e_commerce.features.product.domain.entities

data class FlashSaleItem(
    val id: Int,
    val productId: Int,
    val flashPrice: Double,
    val originalPrice: Double,
    val stock: Long,
    val sold: Int
)
