package com.fathan.e_commerce.di

import com.fathan.e_commerce.data.local.FavoriteDao
import com.fathan.e_commerce.data.repository.FavoriteRepositoryImpl
import com.fathan.e_commerce.data.repository.ProductRepositoryImpl
import com.fathan.e_commerce.domain.repository.FavoriteRepository
import com.fathan.e_commerce.domain.repository.ProductRepository
import com.fathan.e_commerce.domain.usecase.products.GetFavoritesUseCase
import com.fathan.e_commerce.domain.usecase.products.GetProductsUseCase
import com.fathan.e_commerce.domain.usecase.products.IsFavoriteUseCase
import com.fathan.e_commerce.domain.usecase.products.ToggleFavoriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFavoriteRepository(
        dao: FavoriteDao
    ): FavoriteRepository = FavoriteRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(
        repo: FavoriteRepository
    ): ToggleFavoriteUseCase = ToggleFavoriteUseCase(repo)

    @Provides
    @Singleton
    fun provideIsFavoriteUseCase(
        repo: FavoriteRepository
    ): IsFavoriteUseCase = IsFavoriteUseCase(repo)

    @Provides
    @Singleton
    fun provideGetFavoritesUseCase(
        repo: FavoriteRepository
    ): GetFavoritesUseCase = GetFavoritesUseCase(repo)

    @Provides
    @Singleton
    fun provideProductRepository(): ProductRepository = ProductRepositoryImpl()

    @Provides
    @Singleton
    fun provideGetProductsUseCase(
        productRepository: ProductRepository
    ): GetProductsUseCase = GetProductsUseCase(productRepository)
}
