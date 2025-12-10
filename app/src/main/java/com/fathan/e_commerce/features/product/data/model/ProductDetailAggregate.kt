package com.fathan.e_commerce.features.product.data.model

import com.fathan.e_commerce.features.product.domain.entities.Product

// A small aggregate class for ViewModel convenience
data class ProductDetailAggregate(
    val product: Product,
    val images: List<String> = emptyList(),
    val variants: List<ProductVariantDto> = emptyList(),
    val flashSale: FlashSaleDto? = null,
    val avgRating: Double = 0.0,
    val reviewCount: Int = 0,
    val recommended: List<RecommendedDto> = emptyList()
)

data class RecommendedDto(val id: Int, val name: String, val price: Long, val thumbnail: String)
