package com.fathan.e_commerce.data.repository

import com.fathan.e_commerce.data.local.WishlistDao
import com.fathan.e_commerce.data.local.WishlistCollectionEntity
import com.fathan.e_commerce.data.local.WishlistItemEntity
import com.fathan.e_commerce.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// @Inject constructor sangat penting agar Hilt bisa otomatis membuatnya
class WishlistRepositoryImpl @Inject constructor(
    private val wishlistDao: WishlistDao
): WishlistRepository {
    override fun getCollections(): Flow<List<WishlistCollectionEntity>> = wishlistDao.getCollections()

    override suspend fun createCollection(name: String) {
        wishlistDao.insertCollection(WishlistCollectionEntity(name = name))
    }

    override fun getItemsByCollection(collectionId: Int): Flow<List<WishlistItemEntity>> =
        wishlistDao.getItemsByCollection(collectionId)

    override suspend fun addItemToCollection(item: WishlistItemEntity) {
        wishlistDao.insertItem(item)
    }

    override suspend fun getCollectionPreview(collectionId: Int): String? {
        TODO("Not yet implemented")
    }

    override suspend fun getCollectionItemCount(collectionId: Int): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getPreviewImage(collectionId: Int): String? {
        return wishlistDao.getPreviewImage(collectionId)
    }

    override suspend fun getItemCount(collectionId: Int): Int {
        return wishlistDao.getItemCount(collectionId)
    }

    override suspend fun deleteItem(productId: Int) {
        wishlistDao.deleteItem(productId)
    }

    override suspend fun updateCollectionName(collectionId: Int, newName: String) {
        wishlistDao.updateCollectionName(collectionId, newName)
    }

    override suspend fun deleteCollection(collectionId: Int) {
        wishlistDao.deleteItemsByCollection(collectionId)
        wishlistDao.deleteCollection(collectionId)
    }

    override fun isProductInWishlist(productId: Int): Flow<Boolean> = wishlistDao.isProductInWishlist(productId)

}
