package com.fathan.e_commerce.features.product.domain.repository

import com.fathan.e_commerce.features.product.data.model.ProductDetailAggregate
import com.fathan.e_commerce.features.product.domain.entities.Category
import com.fathan.e_commerce.features.product.domain.entities.FlashSaleWithProduct
import com.fathan.e_commerce.features.product.domain.entities.Product
import com.fathan.e_commerce.features.product.domain.entities.ProductFilter

interface ProductRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getProducts(filter: ProductFilter = ProductFilter()): Result<List<Product>>
    suspend fun getFlashSaleProducts(limit: Int): Result<List<FlashSaleWithProduct>>
    suspend fun searchProducts(query: String, sellerId: Long? = null): Result<List<Product>>

    suspend fun getProductDetail(productId: Int): ProductDetailAggregate

}