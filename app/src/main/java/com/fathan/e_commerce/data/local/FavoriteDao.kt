package com.fathan.e_commerce.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(entity: FavoriteEntity)

    @Query("SELECT * from favorites")
    fun getFavorite(): Flow<List<FavoriteEntity>>

    @Query("SELECT Count(*) From favorites WHERE productId = :productId")
    fun isFavorite(productId: Int): Flow<Int>

}