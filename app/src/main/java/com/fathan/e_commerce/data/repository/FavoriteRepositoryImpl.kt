package com.fathan.e_commerce.data.repository

import com.fathan.e_commerce.data.local.FavoriteDao
import com.fathan.e_commerce.data.local.FavoriteEntity
import com.fathan.e_commerce.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao
): FavoriteRepository {
    override suspend fun toggleFavorite(productId: Int) {
        val isFav = dao.isFavorite(productId).map { it > 0 }

        if (isFav.first()){
            dao.deleteFavorite(FavoriteEntity(productId))
        }else{
            dao.insertFavorite(FavoriteEntity(productId))
        }
    }

    override fun isFavorite(productId: Int): Flow<Boolean> =
        dao.isFavorite(productId).map { it > 0 }

    override fun getFavorites(): Flow<List<Int>> = dao.getFavorite().map { list -> list.map { it.productId } }

}