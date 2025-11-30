package com.fathan.e_commerce.domain.repository

import com.fathan.e_commerce.data.local.WishlistCollectionEntity
import com.fathan.e_commerce.data.local.WishlistItemEntity
import kotlinx.coroutines.flow.Flow

interface WishlistRepository {
    fun getCollections(): Flow<List<WishlistCollectionEntity>>
    suspend fun createCollection(name: String)
    fun getItemsByCollection(collectionId: Int): Flow<List<WishlistItemEntity>>
    suspend fun addItemToCollection(item: WishlistItemEntity)
    suspend fun getCollectionPreview(collectionId: Int): String?
    suspend fun getCollectionItemCount(collectionId: Int): Int
    suspend fun getPreviewImage(collectionId: Int): String?
    suspend fun getItemCount(collectionId: Int): Int
    suspend fun deleteItem(productId: Int)
    suspend fun updateCollectionName(collectionId: Int, newName: String)
    suspend fun deleteCollection(collectionId: Int)
    fun isProductInWishlist(productId: Int): Flow<Boolean>

}
