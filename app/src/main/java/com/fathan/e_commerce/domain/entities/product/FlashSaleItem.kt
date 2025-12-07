package com.fathan.e_commerce.domain.entities.product

data class FlashSaleItem(
    val id: Int,
    val productId: Int,
    val flashPrice: Double,
    val originalPrice: Double,
    val stock: Long,
    val sold: Int
)
