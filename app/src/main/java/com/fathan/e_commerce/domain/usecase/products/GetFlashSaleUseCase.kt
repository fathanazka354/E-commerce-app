package com.fathan.e_commerce.domain.usecase.products

import com.fathan.e_commerce.domain.entities.product.FlashSaleWithProduct
import com.fathan.e_commerce.domain.repository.ProductRepository

class GetFlashSaleUseCase(private val repo: ProductRepository) {
    suspend operator fun invoke(limit: Int = 20) : Result<List<FlashSaleWithProduct>> =
        repo.getFlashSaleProducts(limit)
}
