package com.fathan.e_commerce.features.product.domain.entities

data class ProductFilter(
    val categoryId: Long? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sellerId: Long? = null,
    val query: String? = null,
    val limit: Int? = null,
    val offset: Int? = null
)