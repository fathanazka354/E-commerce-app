package com.fathan.e_commerce.features.product.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FlashSaleDto(
    val id: Int,
    val product_id: Long,
    val flash_price: Double? = 0.0,
    val original_price: Double? = 0.0,
    val flash_stock: Long? = 0L,
    val sold_qty: Double? = 0.0,
    val start_at: String? = null,
    val end_at: String? = null
)
