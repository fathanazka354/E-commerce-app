package com.fathan.e_commerce.features.product.domain.usecase

import com.fathan.e_commerce.features.product.domain.entities.Category
import com.fathan.e_commerce.features.product.domain.repository.ProductRepository

class GetCategoriesUseCase(private val repo: ProductRepository) {
    suspend operator fun invoke() : Result<List<Category>> = repo.getCategories()
}
