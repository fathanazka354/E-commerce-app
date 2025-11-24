package com.fathan.e_commerce.domain.repository
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    suspend fun toggleFavorite(productId: Int)
    fun isFavorite(productId: Int): Flow<Boolean>
    fun getFavorites(): Flow<List<Int>>
}