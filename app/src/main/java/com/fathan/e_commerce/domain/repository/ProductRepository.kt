package com.fathan.e_commerce.domain.repository

import com.fathan.e_commerce.domain.entities.product.Category
import com.fathan.e_commerce.domain.entities.product.FlashSaleWithProduct
import com.fathan.e_commerce.domain.entities.product.Product
import com.fathan.e_commerce.domain.model.Product as model
data class ProductFilter(
    val categoryId: Long? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sellerId: Long? = null,
    val query: String? = null,
    val limit: Int? = null,
    val offset: Int? = null
)

interface ProductRepository {
    suspend fun getProducts(): List<model>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getProducts(filter: ProductFilter = ProductFilter()): Result<List<Product>>
    suspend fun getFlashSaleProducts(limit: Int): Result<List<FlashSaleWithProduct>>
    suspend fun searchProducts(query: String, sellerId: Long? = null): Result<List<Product>>
}