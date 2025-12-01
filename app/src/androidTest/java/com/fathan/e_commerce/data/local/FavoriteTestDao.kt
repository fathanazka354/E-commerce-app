package com.fathan.e_commerce.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteTestDao {

    private var db: AppDatabase? = null
    private var dao: FavoriteDao? = null


    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db!!.favoriteDao()
    }

    @After
    fun tearDown() {
        db?.close()
        db = null
        dao = null
    }

    @Test
    fun insertFavorite_ShouldAppearInGetFavorites() = runTest {
        val entity = FavoriteEntity(1)
        dao!!.insertFavorite(entity)

        val favorites = dao!!.getFavorite().first()

        assertEquals(1, favorites.size)
        assertEquals(1, favorites[0].id)
    }

    @Test
    fun deleteFavorite_ShouldNotAppearInGetFavorites() = runTest {
        val entity = FavoriteEntity(1)
        dao!!.insertFavorite(entity)
        dao!!.deleteFavorite(entity)

        val favorites = dao!!.getFavorite().first()

        assertTrue(favorites.isEmpty())
    }

    @Test
    fun isFavorite_ShouldReturnCountCorrectly() = runTest {
        val entity = FavoriteEntity(10)
        dao!!.insertFavorite(entity)

        val count = dao!!.isFavorite(10).first()
        val countOther = dao!!.isFavorite(20).first()

        assertEquals(1, count)
        assertNotEquals(1, countOther)
    }
}
