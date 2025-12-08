package com.fathan.e_commerce.data.repository

import com.fathan.e_commerce.data.local.FavoriteDao
import com.fathan.e_commerce.data.local.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoriteRepositoryImplTest {
    class FakeFavoriteDao : FavoriteDao {
        private val favoritesFlow = MutableStateFlow<List<FavoriteEntity>>(emptyList())
        override suspend fun insertFavorite(entity: FavoriteEntity) {
            val current = favoritesFlow.value.toMutableList()
            if (current.none { it.id == entity.id }) {
                current.add(entity)
                favoritesFlow.value = current
            }
        }

        override suspend fun deleteFavorite(entity: FavoriteEntity) {
            val current = favoritesFlow.value.toMutableList()
            current.removeAll { it.id == entity.id }
            favoritesFlow.value = current
        }

        override fun getFavorite(): Flow<List<FavoriteEntity>> = favoritesFlow

        override fun isFavorite(id: Int): Flow<Int> =
            favoritesFlow.map { list ->
                if (list.any { it.id == id }) 1 else 0
            }

    }

    private lateinit var fakeDao: FakeFavoriteDao
    private lateinit var repository: FavoriteRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeFavoriteDao()
        repository = FavoriteRepositoryImpl(fakeDao)
    }

//    @Test
//    fun toggleFavorite_addsWhenNotFavorite() = runTest {
//        // awal belum favorite
//        assertFalse(repository.isFavorite(1).first())
//
//        repository.toggleFavorite(1)
//
//        assertTrue(repository.isFavorite(1).first())
//        val favIds = repository.getFavorites().first()
//        assertEquals(listOf(1), favIds)
//    }
//
//    @Test
//    fun toggleFavorite_removesWhenAlreadyFavorite() = runTest {
//        // set dulu menjadi favorite
//        repository.toggleFavorite(1)
//        assertTrue(repository.isFavorite(1).first())
//
//        // toggle lagi -> harusnya remove
//        repository.toggleFavorite(1)
//
//        assertFalse(repository.isFavorite(1).first())
//        val favIds = repository.getFavorites().first()
//        assertTrue(favIds.isEmpty())
//    }
}