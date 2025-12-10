package com.fathan.e_commerce.features.product.data.model

// Model Produk Promo
data class PromoProduct(
    val id: Int,
    val name: String,val price: Double,
    val originalPrice: Double,
    val discountPercentage: Int,
    val imageUrl: String,
    val soldCount: Int,
    val rating: Double,
    val shopName: String,
    val isFlashSale: Boolean = false,
    val category: String = "Semua Promo" // Makanan, Kesehatan, dll
)

// Model untuk Banner/Voucher
data class PromoVoucher(
    val id: Int,
    val title: String,
    val subtitle: String,
    val code: String,
    val discountText: String,
    val endDate: String
)
