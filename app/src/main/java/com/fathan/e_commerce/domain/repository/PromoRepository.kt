package com.fathan.e_commerce.domain.repository

import com.fathan.e_commerce.features.product.data.model.PromoProduct
import com.fathan.e_commerce.features.product.data.model.PromoVoucher
import kotlinx.coroutines.flow.Flow

interface PromoRepository {

    // Dummy Data Produk Promo
    fun getPromoProducts(): Flow<List<PromoProduct>>

    // Dummy Data Produk Lokal
    fun getLocalProducts(): Flow<List<PromoProduct>>

    // Dummy Vouchers
    fun getVouchers(): Flow<List<PromoVoucher>>
    // Dummy Data Flash Sale
    fun getFlashSaleProducts(): Flow<List<PromoProduct>>
}
