package com.fathan.e_commerce.data.repository

import android.util.Log
import com.fathan.e_commerce.data.remote.ProductRemoteDataSource
import com.fathan.e_commerce.data.utils.toDomain
import com.fathan.e_commerce.domain.entities.product.Category
import com.fathan.e_commerce.domain.entities.product.FlashSaleItem
import com.fathan.e_commerce.domain.entities.product.FlashSaleWithProduct
import com.fathan.e_commerce.domain.entities.product.Product
import com.fathan.e_commerce.domain.model.DummyData
import com.fathan.e_commerce.domain.model.Product as model
import com.fathan.e_commerce.domain.repository.ProductFilter
import com.fathan.e_commerce.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val remote: ProductRemoteDataSource
)  : ProductRepository {
    override suspend fun getProducts(): List<model> {
        return DummyData.products
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val list = remote.fetchCategories().map { it.toDomain() }
            Log.d("ProductRepositoryImpl", "getCategories: ${list.size}")
            Result.success(list)
        } catch (t: Throwable) {
            Log.e("ProductRepositoryImpl", "getCategories: ${t.message}", )
            Result.failure(t)
        }
    }

    override suspend fun getProducts(filter: ProductFilter): Result<List<Product>> {
        return try {
            val list = remote.fetchProducts(
                categoryId = filter.categoryId,
                minPrice = filter.minPrice,
                maxPrice = filter.maxPrice,
                sellerId = filter.sellerId?.toLong(),
                query = filter.query,
                limit = filter.limit,
                offset = filter.offset
            ).map { it.toDomain() }

            Result.success(list)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun getFlashSaleProducts(limit: Int): Result<List<FlashSaleWithProduct>> {
        return try {
            // 1) Ambil semua flash sale item
            val flashItems = remote.fetchFlashSales(limit).map { dto ->
                FlashSaleItem(
                    id = dto.id.toInt(),
                    productId = dto.product_id.toInt(),
                    flashPrice = dto.flash_price ?: 0.0,
                    originalPrice = dto.original_price ?: 0.0,
                    stock = dto.flash_stock ?: 0L,
                    sold = (dto.sold_qty ?: 0.0).toInt()
                )
            }

            // 2) Ambil product IDs
            val productIds = flashItems.map { it.productId }

            // 3) Fetch all products by IDs (lebih efisien daripada loop)
            val products = remote.fetchProductsByIds(productIds)

            // 4) Map productId → Product
            val productMap = products.associateBy { it.id }

            // 5) Gabungkan FlashSaleItem + Product dalam 1 entity
            val combined = flashItems.mapNotNull { flash ->
                val product = productMap[flash.productId]
                if (product != null) FlashSaleWithProduct(flash, product.toDomain())
                else null // skip kalau product tidak ditemukan
            }

            Result.success(combined)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun searchProducts(query: String, sellerId: Long?): Result<List<Product>> {
        return try {
            val list = remote.searchProducts(query, sellerId?.toLong()).map { it.toDomain() }
            Result.success(list)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}