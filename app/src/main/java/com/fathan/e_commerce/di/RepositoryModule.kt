package com.fathan.e_commerce.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.fathan.e_commerce.data.local.FavoriteDao
import com.fathan.e_commerce.data.local.WishlistDao
import com.fathan.e_commerce.data.remote.ProductRemoteDataSource
import com.fathan.e_commerce.data.remote.SupabaseUserRemoteDataSource
import com.fathan.e_commerce.data.repository.AuthRepositoryImpl
import com.fathan.e_commerce.data.repository.ChatRepositoryImpl
import com.fathan.e_commerce.data.repository.FavoriteRepositoryImpl
import com.fathan.e_commerce.data.repository.ProductRepositoryImpl
import com.fathan.e_commerce.data.repository.PromoRepositoryImpl
import com.fathan.e_commerce.data.repository.WishlistRepositoryImpl
import com.fathan.e_commerce.domain.repository.AuthRepository
import com.fathan.e_commerce.domain.repository.ChatRepository
import com.fathan.e_commerce.domain.repository.FavoriteRepository
import com.fathan.e_commerce.domain.repository.ProductRepository
import com.fathan.e_commerce.domain.repository.PromoRepository
import com.fathan.e_commerce.domain.repository.WishlistRepository
import com.fathan.e_commerce.domain.usecase.auth.LogoutUseCase
import com.fathan.e_commerce.domain.usecase.chats.GetMessagesUseCase
import com.fathan.e_commerce.domain.usecase.chats.SendAudioMessageUseCase
import com.fathan.e_commerce.domain.usecase.chats.SendImageMessageUseCase
import com.fathan.e_commerce.domain.usecase.chats.SendTextMessageUseCase
import com.fathan.e_commerce.domain.usecase.products.GetCategoriesUseCase
import com.fathan.e_commerce.domain.usecase.products.GetFavoritesUseCase
import com.fathan.e_commerce.domain.usecase.products.GetFlashSaleUseCase
import com.fathan.e_commerce.domain.usecase.products.GetProductsUseCase
import com.fathan.e_commerce.domain.usecase.products.GetProductsUseCaseV2
import com.fathan.e_commerce.domain.usecase.products.IsFavoriteUseCase
import com.fathan.e_commerce.domain.usecase.products.SearchProductsUseCase
import com.fathan.e_commerce.domain.usecase.products.ToggleFavoriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
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
    fun provideProductRepository(
        remoteDataSource: ProductRemoteDataSource
    ): ProductRepository = ProductRepositoryImpl(remoteDataSource)

    @Provides
    @Singleton
    fun provideGetProductsUseCase(
        productRepository: ProductRepository
    ): GetProductsUseCase = GetProductsUseCase(productRepository)

    @Provides
    @Singleton
    fun provideGetProductsV2UseCase(
        productRepository: ProductRepository
    ): GetProductsUseCaseV2 = GetProductsUseCaseV2(productRepository)

    @Provides
    @Singleton
    fun provideGetCategoriesUseCase(
        productRepository: ProductRepository
    ): GetCategoriesUseCase = GetCategoriesUseCase(productRepository)

    @Provides
    @Singleton
    fun provideFlashSaleItemsUseCase(
        productRepository: ProductRepository
    ): GetFlashSaleUseCase = GetFlashSaleUseCase(productRepository)

    @Provides
    @Singleton
    fun provideSearchProductsUseCase(
        productRepository: ProductRepository
    ): SearchProductsUseCase = SearchProductsUseCase(productRepository)

    @Provides
    @Singleton
    fun provideLogoutUseCase(
        authRepository: AuthRepository
    ): LogoutUseCase = LogoutUseCase(authRepository)


    @Provides    @Singleton
    fun provideWishlistRepository(wishlistDao: WishlistDao): WishlistRepository {
        return WishlistRepositoryImpl(wishlistDao)
    }


    @Provides    @Singleton
    fun providePromoRepository(): PromoRepository {
        return PromoRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: Auth,
        postgrest: Postgrest,
        remoteDataSource: SupabaseUserRemoteDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(
            auth,
            postgrest = postgrest,
            remoteDataSource = remoteDataSource
        )
    }

}
