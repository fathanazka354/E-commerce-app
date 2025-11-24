package com.fathan.e_commerce.di

import android.content.Context
import androidx.room.Room
import com.fathan.e_commerce.data.UserPreferences
import com.fathan.e_commerce.data.local.AppDatabase
import com.fathan.e_commerce.data.local.FavoriteDao
import com.fathan.e_commerce.data.repository.ProductRepositoryImpl
import com.fathan.e_commerce.domain.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext ctx: Context
    ): AppDatabase = Room.databaseBuilder(
        ctx,
        AppDatabase::class.java,
        "app_db"
    ).build()

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

}