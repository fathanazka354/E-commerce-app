package com.fathan.e_commerce.features.product.domain.usecase

import com.fathan.e_commerce.features.product.domain.entities.FlashSaleWithProduct
import com.fathan.e_commerce.features.product.domain.repository.ProductRepository

class GetFlashSaleUseCase(private val repo: ProductRepository) {
    suspend operator fun invoke(limit: Int = 20) : Result<List<FlashSaleWithProduct>> =
        repo.getFlashSaleProducts(limit)
}
