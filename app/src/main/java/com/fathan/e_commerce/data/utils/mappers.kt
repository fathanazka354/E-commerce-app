package com.fathan.e_commerce.data.utils

import com.fathan.e_commerce.domain.entities.product.FlashSaleItem


import com.fathan.e_commerce.data.models.CategoryDto
import com.fathan.e_commerce.data.models.FlashSaleDto
import com.fathan.e_commerce.data.models.ProductDto
import com.fathan.e_commerce.domain.entities.product.Category
import com.fathan.e_commerce.domain.entities.product.Product

fun CategoryDto.toDomain(): Category =
    Category(id = id, name = name ?: "Unknown", iconEmoji = icon ?: "📦")

fun ProductDto.toDomain(): Product =
    Product(
        id = id.toInt(),
        name = name,
        brand = brand ?: "",
        price = price ?: 0.0,
        storeId = store_id?.toInt() ?: 0,
        sellerId = seller_id?.toInt() ?: 0,
        rating = (rate ?: 0.0).toFloat()
    )

fun FlashSaleDto.toDomain(product: Product? = null): FlashSaleItem =
    FlashSaleItem(
        id = id.toInt(),
        productId = product?.id ?: product_id.toInt(),
        flashPrice = flash_price ?: 0.0,
        originalPrice = original_price ?: 0.0,
        stock = flash_stock ?: 0L,
        sold = (sold_qty ?: 0.0).toInt()
    )
