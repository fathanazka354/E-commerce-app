package com.fathan.e_commerce.domain.usecase.products

import com.fathan.e_commerce.domain.entities.product.Product
import com.fathan.e_commerce.domain.entities.product.ProductFilter
import com.fathan.e_commerce.domain.repository.ProductRepository

class GetProductsUseCaseV2(private val repo: ProductRepository) {
    suspend operator fun invoke(filter: ProductFilter) : Result<List<Product>> =
        repo.getProducts(filter)
}
