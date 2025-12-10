package com.fathan.e_commerce.features.product.domain.entities

data class ProductVariant(
    val id: Int,
    val name: String,
    val price: Long?,
    val stock: Int?
)
