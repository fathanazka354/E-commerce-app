package com.fathan.e_commerce.features.product.domain.entities

data class FlashSaleWithProduct(
    val flash: FlashSaleItem,
    val product: Product
)
