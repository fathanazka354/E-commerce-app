package com.fathan.e_commerce.domain.entities.product

data class FlashSaleWithProduct(
    val flash: FlashSaleItem,
    val product: Product
)
