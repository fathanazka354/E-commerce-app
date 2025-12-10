package com.fathan.e_commerce.features.product.domain.usecase

import com.fathan.e_commerce.features.product.domain.entities.Product
import com.fathan.e_commerce.features.product.domain.repository.ProductRepository

class SearchProductsUseCase(private val repo: ProductRepository) {
    suspend operator fun invoke(query: String, sellerId: Long? = null) : Result<List<Product>> =
        repo.searchProducts(query, sellerId)
}
