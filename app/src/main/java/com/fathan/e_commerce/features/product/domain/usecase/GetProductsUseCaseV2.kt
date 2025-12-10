package com.fathan.e_commerce.features.product.domain.usecase

import com.fathan.e_commerce.features.product.domain.entities.Product
import com.fathan.e_commerce.features.product.domain.entities.ProductFilter
import com.fathan.e_commerce.features.product.domain.repository.ProductRepository

class GetProductsUseCaseV2(private val repo: ProductRepository) {
    suspend operator fun invoke(filter: ProductFilter) : Result<List<Product>> =
        repo.getProducts(filter)
}
