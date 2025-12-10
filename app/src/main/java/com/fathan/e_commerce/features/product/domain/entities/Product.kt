package com.fathan.e_commerce.features.product.domain.entities

data class Product(
    val id: Int,
    val name: String,
//    val brand: String,
    val price: Double,
    val storeId: Int,
    val sellerId: Int,
    val rating: Float,
    val thumbnail: String?,
    val description: String?,
    val sold: Int?,
){
    companion object {
        fun empty() = Product(0, "", 0.0, 0, 0, 0f, null, null, 0)
    }
}

