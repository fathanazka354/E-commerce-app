package com.fathan.e_commerce.domain.usecase.products

import com.fathan.e_commerce.domain.entities.product.Product
import com.fathan.e_commerce.domain.repository.ProductRepository

class SearchProductsUseCase(private val repo: ProductRepository) {
    suspend operator fun invoke(query: String, sellerId: Long? = null) : Result<List<Product>> =
        repo.searchProducts(query, sellerId)
}
