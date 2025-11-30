package com.fathan.e_commerce.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.fathan.e_commerce.data.local.FavoriteDao
import com.fathan.e_commerce.data.local.WishlistDao
import com.fathan.e_commerce.data.repository.ChatRepositoryImpl
import com.fathan.e_commerce.data.repository.FavoriteRepositoryImpl
import com.fathan.e_commerce.data.repository.ProductRepositoryImpl
import com.fathan.e_commerce.data.repository.WishlistRepositoryImpl
import com.fathan.e_commerce.domain.repository.ChatRepository
import com.fathan.e_commerce.domain.repository.FavoriteRepository
import com.fathan.e_commerce.domain.repository.ProductRepository
import com.fathan.e_commerce.domain.repository.WishlistRepository
import com.fathan.e_commerce.domain.usecase.chats.GetMessagesUseCase
import com.fathan.e_commerce.domain.usecase.chats.SendAudioMessageUseCase
import com.fathan.e_commerce.domain.usecase.chats.SendImageMessageUseCase
import com.fathan.e_commerce.domain.usecase.chats.SendTextMessageUseCase
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

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideMessageRepository(
        dao: FavoriteDao
    ): ChatRepository = ChatRepositoryImpl()

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
    fun provideGetChatsUseCase(
        repo: ChatRepository
    ): GetMessagesUseCase = GetMessagesUseCase(repo)


    @Provides
    @Singleton
    fun provideSendChatUseCase(
        repo: ChatRepository
    ): SendTextMessageUseCase = SendTextMessageUseCase(repo)

    @Provides
    @Singleton
    fun provideSendImageChatUseCase(
        repo: ChatRepository
    ): SendImageMessageUseCase = SendImageMessageUseCase(repo)

    @Provides
    @Singleton
    fun provideSendAudioChatUseCase(
        repo: ChatRepository
    ): SendAudioMessageUseCase = SendAudioMessageUseCase(repo)

    @Provides
    @Singleton
    fun provideProductRepository(): ProductRepository = ProductRepositoryImpl()

    @Provides
    @Singleton
    fun provideGetProductsUseCase(
        productRepository: ProductRepository
    ): GetProductsUseCase = GetProductsUseCase(productRepository)

    @Provides    @Singleton
    fun provideWishlistRepository(wishlistDao: WishlistDao): WishlistRepository {
        return WishlistRepositoryImpl(wishlistDao)
    }
}
