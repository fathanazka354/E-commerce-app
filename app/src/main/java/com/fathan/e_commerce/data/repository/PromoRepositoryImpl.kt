package com.fathan.e_commerce.data.repository

import com.fathan.e_commerce.data.models.PromoProduct
import com.fathan.e_commerce.data.models.PromoVoucher
import com.fathan.e_commerce.domain.repository.PromoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PromoRepositoryImpl @Inject constructor(): PromoRepository {

    // Dummy Data Produk Promo
    override fun getPromoProducts(): Flow<List<PromoProduct>> = flow {
        val products = listOf(
            PromoProduct(1, "LEKI - ULTRATRAIL FX.ONE", 2805300.0, 3300000.0, 17, "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=500", 21, 5.0, "Larilari.id", category = "Olahraga"),
            PromoProduct(2, "Aonijie Soft Flask SD31 500ml", 85500.0, 90000.0, 5, "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=500", 500, 5.0, "Aonijie Indo", category = "Makanan & Minuman"),
            PromoProduct(3, "Tas Hydropack Trail Running", 254998.0, 600000.0, 58, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500", 100, 4.8, "Bogaboo", isFlashSale = true, category = "Olahraga"),
            PromoProduct(4, "Xiaomi Watch S4 41mm AMOLED", 1811920.0, 2499000.0, 27, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500", 50, 4.9, "Xiaomi Official", category = "Elektronik"),
            PromoProduct(5, "Batik Pria Lengan Panjang", 156000.0, 300000.0, 48, "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500", 1200, 4.7, "Batik Keris", category = "Fashion"),
            PromoProduct(6, "Dyson Airwrap Complete", 12499000.0, 14000000.0, 10, "https://images.unsplash.com/photo-1522338242992-e1a54906a8ae?w=500", 5, 5.0, "Dyson Indo", category = "Elektronik")
        )
        emit(products)
    }

    // Dummy Data Produk Lokal
    override fun getLocalProducts(): Flow<List<PromoProduct>> = flow {
        val products = listOf(
            PromoProduct(11, "Gitar Hard Rock HR-103", 405999.0, 800000.0, 49, "https://images.unsplash.com/photo-1516924962500-2b4b3b99ea02?w=500", 100, 4.8, "HardRock", category = "Lokal"),
            PromoProduct(12, "Buku Amazing Kalimat Thayyibah", 56600.0, 120000.0, 53, "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=500", 23, 5.0, "Flava Bookstore", category = "Lokal"),
            PromoProduct(13, "Kemeja Batik Modern", 125000.0, 250000.0, 50, "https://images.unsplash.com/photo-1487222477894-8943e31ef7b2?w=500", 500, 4.9, "Batik Solo", category = "Lokal"),
            PromoProduct(14, "Tas Anyaman Rotan", 85000.0, 100000.0, 15, "https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=500", 80, 4.7, "Bali Craft", category = "Lokal")
        )
        emit(products)
    }

    // Dummy Vouchers
    override fun getVouchers(): Flow<List<PromoVoucher>> = flow {
        emit(listOf(
            PromoVoucher(1, "Diskon 8% s.d 10rb", "Min. belanja Rp80rb", "LOKAL8", "8%", "00:00:00"),
            PromoVoucher(2, "Cashback 15%", "Min. belanja Rp50rb", "LOKALCB", "15%", "2 Hari")
        ))
    }

    // Dummy Data Flash Sale
    override fun getFlashSaleProducts(): Flow<List<PromoProduct>> = flow {
        val list = listOf(
            PromoProduct(21, "TWS Bluetooth 5.3", 99000.0, 350000.0, 70, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500", 5000, 4.6, "Audio Store", isFlashSale = true),
            PromoProduct(22, "Powerbank 20000mAh", 120000.0, 400000.0, 65, "https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?w=500", 2000, 4.8, "Aukey", isFlashSale = true)
        )
        emit(list)
    }
}