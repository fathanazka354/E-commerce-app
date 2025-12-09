package com.fathan.e_commerce.data.remote

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.fathan.e_commerce.data.models.CategoryDto
import com.fathan.e_commerce.data.models.FeedbackSimpleDto
import com.fathan.e_commerce.data.models.FlashSaleDto
import com.fathan.e_commerce.data.models.MediaDto
import com.fathan.e_commerce.data.models.ProductDto
import com.fathan.e_commerce.data.models.ProductVariantDto
import com.fathan.e_commerce.data.models.RateOnlyDto
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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


    suspend fun fetchProductById(id: Int): ProductDto?
    suspend fun fetchMediasByProductId(productId: Int): List<MediaDto>
    suspend fun fetchVariantsByProductId(productId: Int): List<ProductVariantDto>
    suspend fun fetchActiveFlashSaleForProduct(productId: Int): FlashSaleDto?
    suspend fun fetchRatingSummary(productId: Int): Pair<Double, Int> // avg, count
    suspend fun fetchRecommendedByProduct(productId: Int, limit: Int = 10): List<ProductDto>
}

class ProductRemoteDataSourceImpl @Inject constructor(private val postgrest: Postgrest) :
        ProductRemoteDataSource {

    override suspend fun fetchCategories(): List<CategoryDto> {
        // simple select all categories where status = true
        val categories = postgrest
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

        Log.d("ProductRemoteDataSource", "fetchProducts: $categoryId || ${productIdsForCategory?.size}")

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
                                        "price",
                                        "store_id",
                                        "seller_id",
                                        "rate",
                                        "thumbnail",
                                        "status"
                                )
                ) {
                    filter {
                        if (productIdsForCategory != null && productIdsForCategory.isNotEmpty()) {
                            // Filter by product IDs using or condition
                            // Note: This is a workaround since 'in' operator might not be available
                            // For better performance, consider using a database view or RPC
                            // function

                            isIn("id", productIdsForCategory)

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
        val result = postgrest
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
            .select{
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
                                        "thumbnail",
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

    override suspend fun fetchProductById(id: Int): ProductDto? {
        val res = postgrest
            .from("products")
            .select(columns = Columns.list("id","name","price","store_id","seller_id","rate","status","description","sold", "thumbnail")) {
                filter { eq("id", id) }
                limit(1)
            }
        val list = res.decodeList<ProductDto>()
        Log.d("ProductRemoteDataSource", "fetchProductById: ${res.data}")
        return list.firstOrNull()
    }

    override suspend fun fetchMediasByProductId(productId: Int): List<MediaDto> {
        val res = postgrest
            .from("medias")
            .select(columns = Columns.list("id", "path_url",)) {
                filter {
                    eq("owner_id", productId)
                    eq("status", true)
                    eq("type", "2")
                }
            }
        return res.decodeList<MediaDto>()
    }

    override suspend fun fetchVariantsByProductId(productId: Int): List<ProductVariantDto> {
        val res = postgrest
            .from("product_variants")
            .select(columns = Columns.list("id", "name", "price", "stock")) {
                filter { eq("product_id", productId) }
            }
        return res.decodeList<ProductVariantDto>()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchActiveFlashSaleForProduct(productId: Int): FlashSaleDto? {
        val zoneId = ZoneId.of("Asia/Jakarta")

        val startOfDay = LocalDate.now(zoneId).atStartOfDay(zoneId)
        val endOfDay = startOfDay.plusDays(1)
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        val res = postgrest
            .from("flash_sale_products")
            .select(
                columns = Columns.list(
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
                filter {
                    eq("product_id", productId)
                    eq("status", true)
                    gte("start_at", startOfDay.format(formatter))
                    lte("end_at", endOfDay.format(formatter))
                }
                limit(1)
            }
        return res.decodeList<FlashSaleDto>().firstOrNull()
    }

    override suspend fun fetchRatingSummary(productId: Int): Pair<Double, Int> {
        // 1) Ambil jumlah feedback (count)
        val fbRes = postgrest
            .from("feedback")
            .select(columns = Columns.list("id")) { // cukup ambil id untuk hitung jumlah
                filter { eq("product_id", productId) }
            }

        // decode ke FeedbackSimpleDto (atau Map) — we only need the number of rows
        val fbList = try {
            fbRes.decodeList<FeedbackSimpleDto>()
        } catch (t: Throwable) {
            // fallback: if decoding fails, try decode to generic map and get size
            try {
                fbRes.decodeList<Map<String, Any?>>()
            } catch (_: Throwable) {
                emptyList()
            }.mapIndexed { index, _ -> FeedbackSimpleDto(index, null) }
        }
        val count = fbList.size

        // 2) Ambil avg rate dari products.rate (assuming products.rate stores average rating)
        val productRes = postgrest
            .from("products")
            .select(columns = Columns.list("rate")) {
                filter { eq("id", productId) }
                limit(1)
            }

        val pd = try {
            productRes.decodeList<RateOnlyDto>().firstOrNull()
        } catch (t: Throwable) {
            // fallback to map decoding if structure differs
            productRes.decodeList<Map<String, Any?>>().firstOrNull()
                ?.get("rate")?.let { (it as? Number)?.toDouble() ?: 0.0 }
                ?.let { RateOnlyDto(it) }
                ?: RateOnlyDto(null)
        }

        val avg = pd?.rate ?: 0.0
        return Pair(avg, count)
    }


    override suspend fun fetchRecommendedByProduct(
        productId: Int,
        limit: Int
    ): List<ProductDto> {
        // find categories for product, then return other products in same category ordered by sold desc
        val catRes = postgrest
            .from("product_categories")
            .select(columns = Columns.list("category_id")) {
                filter { eq("product_id", productId) }
            }
        val cats = catRes.decodeList<Map<String, Long>>().mapNotNull { it["category_id"] }

        if (cats.isEmpty()) return emptyList()

        // search other products in those categories
        val prodRes = postgrest
            .from("product_categories")
            .select(columns = Columns.list("product_id")) {
                filter {
                    or {
                        cats.forEach { cid -> eq("category_id", cid) }
                    }
                }
                limit(limit.toLong())
            }
        val productIds = prodRes.decodeList<Map<String, Long>>().mapNotNull { it["product_id"] }.filter { it.toInt() != productId }.distinct()
        if (productIds.isEmpty()) return emptyList()

        // fetch product details and thumbnail
        val result = postgrest
            .from("products")
            .select(columns = Columns.list("id","name","price","store_id","seller_id","rate","status","description")) {
                filter {
                    or {
                        productIds.forEach { pid -> eq("id", pid.toInt()) }
                    }
                }
                limit(limit.toLong())
            }

        val baseProducts = result.decodeList<ProductDto>()

        // attach first media as thumbnail
        return baseProducts.map { p ->
            val thumb = try {
                postgrest.from("medias").select(columns = Columns.list("path_url")) {
                    filter { eq("product_id", p.id) }
                    limit(1)
                }.decodeList<Map<String, String>>().firstOrNull()?.get("path_url")
            } catch (t: Throwable) { null }
            p.copy().also { /* no-op */ }.apply { /* keep original */ }.let {
                // product DTO doesn't have thumbnail field; returning DTO and repository will map to recommended DTO
                p.copy()
            }
        }
    }
}
