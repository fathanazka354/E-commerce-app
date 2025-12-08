package com.fathan.e_commerce.domain.entities.product

data class ProductVariant(
    val id: Int,
    val name: String,
    val price: Long?,
    val stock: Int?
)
