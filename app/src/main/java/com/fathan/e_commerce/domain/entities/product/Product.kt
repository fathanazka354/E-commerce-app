package com.fathan.e_commerce.domain.entities.product

data class Product(
    val id: Int,
    val name: String,
    val brand: String,
    val price: Double,
    val storeId: Int,
    val sellerId: Int,
    val rating: Float
)
