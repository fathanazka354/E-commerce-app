package com.fathan.e_commerce.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_collections")
    fun getCollections(): Flow<List<WishlistCollectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: WishlistCollectionEntity)

    @Query("SELECT * FROM wishlist_items WHERE collectionId = :collectionId")
    fun getItemsByCollection(collectionId: Int): Flow<List<WishlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE productId = :productId")
    suspend fun deleteItem(productId: Int)

    @Query("SELECT imageUrl FROM wishlist_items WHERE collectionId = :collectionId LIMIT 1")
    suspend fun getPreviewImage(collectionId: Int): String?

    @Query("SELECT COUNT(*) FROM wishlist_items WHERE collectionId = :collectionId")
    suspend fun getItemCount(collectionId: Int): Int

    @Query("UPDATE wishlist_collections SET name = :newName WHERE id = :collectionId")
    suspend fun updateCollectionName(collectionId: Int, newName: String)

    @Query("DELETE FROM wishlist_collections WHERE id = :collectionId")
    suspend fun deleteCollection(collectionId: Int)

    @Query("DELETE FROM wishlist_items WHERE collectionId = :collectionId")
    suspend fun deleteItemsByCollection(collectionId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE productId = :productId)")
    fun isProductInWishlist(productId: Int): Flow<Boolean>


}
