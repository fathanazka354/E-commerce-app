package com.fathan.e_commerce.features.product.domain.usecase

import com.fathan.e_commerce.features.product.domain.repository.ProductRepository

class GetProductsUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke() = repository.getProducts()
}