package com.fathan.e_commerce.features.product.ui

import com.fathan.e_commerce.features.product.domain.entities.FlashSaleItem
import com.fathan.e_commerce.features.product.domain.entities.Product
import com.fathan.e_commerce.features.product.domain.entities.ProductVariant
import com.fathan.e_commerce.features.product.data.model.RecommendedDto

data class ProductDetailUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val product: Product = Product.empty(),
    val images: List<String> = emptyList(),
    val variants: List<ProductVariant> = emptyList(),
    val flashSale: FlashSaleItem? = null,
    val avgRating: Double = 0.0,
    val reviewCount: Int = 0,
    val recommended: List<RecommendedDto> = emptyList(),
    val isFavorite: Boolean = false
) {
    fun copyLoading() = copy(loading = true, error = null)
    fun copyError(msg: String?) = copy(loading = false, error = msg)
}
