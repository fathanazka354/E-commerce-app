package com.fathan.e_commerce.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int,
    val name: String,
    val brand: String? = null,
    val price: Double? = 0.0,
    val store_id: Long? = null,
    val seller_id: Long? = null,
    val rate: Double? = 0.0,
    val status: Boolean? = true
)
