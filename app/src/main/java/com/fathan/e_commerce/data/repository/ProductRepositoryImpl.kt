package com.fathan.e_commerce.data.repository

import android.util.Log
import com.fathan.e_commerce.data.models.FlashSaleDto
import com.fathan.e_commerce.data.models.ProductDetailAggregate
import com.fathan.e_commerce.data.models.ProductVariantDto
import com.fathan.e_commerce.data.models.RecommendedDto
import com.fathan.e_commerce.data.remote.ProductRemoteDataSource
import com.fathan.e_commerce.data.utils.toDomain
import com.fathan.e_commerce.domain.entities.product.Category
import com.fathan.e_commerce.domain.entities.product.FlashSaleItem
import com.fathan.e_commerce.domain.entities.product.FlashSaleWithProduct
import com.fathan.e_commerce.domain.entities.product.Product
import com.fathan.e_commerce.domain.entities.product.ProductFilter
import com.fathan.e_commerce.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val remote: ProductRemoteDataSource
)  : ProductRepository {

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
                sellerId = filter.sellerId,
                query = filter.query,
                limit = filter.limit,
                offset = filter.offset
            ).map { it.toDomain() }

            Log.d("ProductRepositoryImpl", "getProducts: ${list.map { it.thumbnail }}")

            Result.success(list)
        } catch (t: Throwable) {
            Log.e("ProductRepositoryImpl", "getProducts: ${t.message}")
            Result.failure(t)
        }
    }

    override suspend fun getFlashSaleProducts(limit: Int): Result<List<FlashSaleWithProduct>> {
        return try {
            // 1) Ambil semua flash sale item
            val flashItems = remote.fetchFlashSales(limit).map { dto ->
                FlashSaleItem(
                    id = dto.id,
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
            val list = remote.searchProducts(query, sellerId).map { it.toDomain() }
            Result.success(list)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun getProductDetail(productId: Int): ProductDetailAggregate {
        // fetch product
        try {

            val pDtos = remote.fetchProductById(productId)
                ?: throw IllegalStateException("Product not found: $productId")
            val product = pDtos.toDomain()

            // images
            val images = remote.fetchMediasByProductId(productId).mapNotNull { it.path_url }

            // variants
            val variants = remote.fetchVariantsByProductId(productId).map { v ->
                ProductVariantDto(
                    id = v.id,
                    name = v.name ?: "",
                    price = v.price,
                    stock = v.stock
                )
            }

            // flash sale (active) if any
            val fs = remote.fetchActiveFlashSaleForProduct(productId)
            val flashSale = fs?.let { f ->
                FlashSaleDto(
                    id = f.id,
                    product_id = f.product_id,
                    flash_price = (f.flash_price ?: 0.0).toLong().toDouble(),
                    original_price = (f.original_price ?: 0.0).toLong().toDouble()
                )
            }

            // rating summary
            val rating = remote.fetchRatingSummary(productId)
            val avgRating = rating.first
            val reviewCount = rating.second

            // recommended by category
            val recommended = remote.fetchRecommendedByProduct(productId).map {
                RecommendedDto(
                    id = it.id,
                    name = it.name,
                    price = (it.price ?: 0.0).toLong(),
                    thumbnail = it.thumbnail ?: ""
                )
            }

            Log.d("ProductRepositoryImpl", "getProductDetail: ${product}")
            Log.d("ProductRepositoryImpl", "getProductDetail: ${images}")
            Log.d("ProductRepositoryImpl", "getProductDetail: ${variants}")
            Log.d("ProductRepositoryImpl", "getProductDetail: ${flashSale}")
            Log.d("ProductRepositoryImpl", "getProductDetail: ${avgRating}")

            return ProductDetailAggregate(
                product = product,
                images = images,
                variants = variants,
                flashSale = flashSale,
                avgRating = avgRating,
                reviewCount = reviewCount,
                recommended = recommended
            )
        } catch (e: Exception){
            Log.e("ProductRepositoryImpl", "getProductDetail: $e", )
            throw e

        }
    }
}