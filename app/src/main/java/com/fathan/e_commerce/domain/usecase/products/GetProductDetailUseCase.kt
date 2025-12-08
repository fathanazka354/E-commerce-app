package com.fathan.e_commerce.domain.usecase.products

import com.fathan.e_commerce.domain.repository.ProductRepository

class GetProductDetailUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: Int) = repository.getProductDetail(productId)
}