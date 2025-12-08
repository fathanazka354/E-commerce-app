package com.fathan.e_commerce.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductCategoryDto(
    @SerialName("id")
    val id: Long,
    @SerialName("product_id")
    val productId: Long,
    @SerialName("category_id")
    val categoryId: Long,
    @SerialName("created_at")
    val createdAt: String? = null
)