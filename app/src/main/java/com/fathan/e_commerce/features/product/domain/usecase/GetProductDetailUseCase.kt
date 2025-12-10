package com.fathan.e_commerce.features.product.domain.usecase

import com.fathan.e_commerce.features.product.domain.repository.ProductRepository

class GetProductDetailUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: Int) = repository.getProductDetail(productId)
}