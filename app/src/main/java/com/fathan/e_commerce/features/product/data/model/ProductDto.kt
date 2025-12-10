package com.fathan.e_commerce.features.product.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int,
    val name: String,
    val thumbnail: String? = null,
    val description: String? = null,
    val price: Double? = 0.0,
    val sold: Int? = 0,
    val store_id: Long? = null,
    val seller_id: Long? = null,
    val rate: Double? = 0.0,
    val status: Boolean? = true
)
