package com.fathan.e_commerce.data.remote

import android.util.Log
import com.fathan.e_commerce.data.models.CategoryDto
import com.fathan.e_commerce.data.models.FlashSaleDto
import com.fathan.e_commerce.data.models.ProductDto
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject

interface ProductRemoteDataSource {
    suspend fun fetchCategories(): List<CategoryDto>
    suspend fun fetchProducts(
            categoryId: Long? = null,
            minPrice: Double? = null,
            maxPrice: Double? = null,
            sellerId: Long? = null,
            query: String? = null,
            limit: Int? = null,
            offset: Int? = null
    ): List<ProductDto>

    suspend fun fetchProductsByIds(ids: List<Int>): List<ProductDto>
    suspend fun fetchFlashSales(limit: Int = 20): List<FlashSaleDto>
    suspend fun searchProducts(query: String, sellerId: Long? = null): List<ProductDto>
}

class ProductRemoteDataSourceImpl @Inject constructor(private val postgrest: Postgrest) :
        ProductRemoteDataSource {

    override suspend fun fetchCategories(): List<CategoryDto> {
        // simple select all categories where status = true
        var categories = postgrest
            .from("categories")
            .select(columns = Columns.list("id", "name", "icon")) {
                filter { eq("status", true) }
            }
        Log.d("ProductRemoteDataSource", "fetchCategories: ${categories.decodeList<CategoryDto>().size}")
        return categories
                .decodeList()
    }

    override suspend fun fetchProducts(
            categoryId: Long?,
            minPrice: Double?,
            maxPrice: Double?,
            sellerId: Long?,
            query: String?,
            limit: Int?,
            offset: Int?
    ): List<ProductDto> {
        // First, get product IDs for the category if needed
        val productIdsForCategory =
                if (categoryId != null) {
                    postgrest
                            .from("product_categories")
                            .select(columns = Columns.list("product_id")) {
                                filter { eq("category_id", categoryId) }
                            }
                            .decodeList<Map<String, Long>>()
                            .mapNotNull { it["product_id"] }
                } else {
                    null
                }

        // If we have category filter and no products found, return empty list
        if (categoryId != null && (productIdsForCategory == null || productIdsForCategory.isEmpty())
        ) {
            return emptyList()
        }

        return postgrest
                .from("products")
                .select(
                        columns =
                                Columns.list(
                                        "id",
                                        "name",
                                        "brand",
                                        "price",
                                        "store_id",
                                        "seller_id",
                                        "rate",
                                        "status"
                                )
                ) {
                    filter {
                        if (productIdsForCategory != null && productIdsForCategory.isNotEmpty()) {
                            // Filter by product IDs using or condition
                            // Note: This is a workaround since 'in' operator might not be available
                            // For better performance, consider using a database view or RPC
                            // function
                            productIdsForCategory.firstOrNull()?.let { firstId ->
                                eq("id", firstId)
                            }
                            // For multiple IDs, you might need to use a different approach
                            // or make multiple queries and combine results
                        }
                        if (sellerId != null) {
                            eq("seller_id", sellerId)
                        }
                        if (minPrice != null) {
                            gte("price", minPrice)
                        }
                        if (maxPrice != null) {
                            lte("price", maxPrice)
                        }
                        if (!query.isNullOrBlank()) {
                            // simple ilike on name
                            ilike("name", "%$query%")
                        }
                    }
                    if (limit != null) {
                        limit(limit.toLong())
                    }
                    if (offset != null) {
                        range(offset.toLong(), (offset + (limit ?: 0) - 1).toLong())
                    }
                }
                .decodeList()
    }

    override suspend fun fetchFlashSales(limit: Int): List<FlashSaleDto> {
        // Join flash_sale_products with products? For now return flash_sale_products fields
        var result = postgrest
            .from("flash_sale_products")
            .select(
                columns =
                    Columns.list(
                        "id",
                        "product_id",
                        "flash_price",
                        "original_price",
                        "flash_stock",
                        "sold_qty",
                        "start_at",
                        "end_at"
                    )
            ) {
                filter { eq("status", true) }
                limit(limit.toLong())
            }

        Log.d("ProductRemoteDataSource", "fetchFlashSales: ${result.decodeList<FlashSaleDto>().size}")
        return result.decodeList()
    }

    override suspend fun fetchProductsByIds(ids: List<Int>): List<ProductDto> {
        if (ids.isEmpty()) return emptyList()

//        val idList = ids.joinToString(",") // 1,2,3,4,...

        val result = postgrest
            .from("products")
            .select(){
                filter {
                    or {
                        ids.forEach { id ->
                            eq("id", id)
                        }
                    }
                } // gunakan filter IN

            }

        return result.decodeList()
    }


    override suspend fun searchProducts(query: String, sellerId: Long?): List<ProductDto> {
        return postgrest
                .from("products")
                .select(
                        columns =
                                Columns.list(
                                        "id",
                                        "name",
                                        "brand",
                                        "price",
                                        "store_id",
                                        "seller_id",
                                        "rate",
                                        "status"
                                )
                ) {
                    filter {
                        ilike("name", "%$query%")
                        if (sellerId != null) {
                            eq("seller_id", sellerId)
                        }
                    }
                }
                .decodeList()
    }
}
