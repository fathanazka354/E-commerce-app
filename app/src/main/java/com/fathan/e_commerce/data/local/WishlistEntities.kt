package com.fathan.e_commerce.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist_collections")
data class WishlistCollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey val productId: Int, // ID Produk asli
    val collectionId: Int, // Foreign Key ke Collection
    val name: String,
    val price: Double,
    val imageUrl: String
)

