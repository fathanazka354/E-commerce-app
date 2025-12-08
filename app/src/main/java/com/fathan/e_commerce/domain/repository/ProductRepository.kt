package com.fathan.e_commerce.domain.repository

import com.fathan.e_commerce.data.models.ProductDetailAggregate
import com.fathan.e_commerce.domain.entities.product.Category
import com.fathan.e_commerce.domain.entities.product.FlashSaleWithProduct
import com.fathan.e_commerce.domain.entities.product.Product
import com.fathan.e_commerce.domain.entities.product.ProductFilter


interface ProductRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getProducts(filter: ProductFilter = ProductFilter()): Result<List<Product>>
    suspend fun getFlashSaleProducts(limit: Int): Result<List<FlashSaleWithProduct>>
    suspend fun searchProducts(query: String, sellerId: Long? = null): Result<List<Product>>

    suspend fun getProductDetail(productId: Int): ProductDetailAggregate

}