package com.fathan.e_commerce.domain.usecase.products

import com.fathan.e_commerce.domain.entities.product.Category
import com.fathan.e_commerce.domain.repository.ProductRepository

class GetCategoriesUseCase(private val repo: ProductRepository) {
    suspend operator fun invoke() : Result<List<Category>> = repo.getCategories()
}
