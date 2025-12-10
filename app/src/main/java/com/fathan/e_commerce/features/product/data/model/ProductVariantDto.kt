package com.fathan.e_commerce.features.product.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductVariantDto(
    val id: Int,
    val name: String? = null,
    val price: Long? = null,
    val stock: Int? = null
)