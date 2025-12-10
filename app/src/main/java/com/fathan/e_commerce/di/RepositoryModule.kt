package com.fathan.e_commerce.di

import com.fathan.e_commerce.data.local.FavoriteDao
import com.fathan.e_commerce.data.local.WishlistDao
import com.fathan.e_commerce.features.product.data.source.ProductRemoteDataSource
import com.fathan.e_commerce.data.remote.SupabaseUserRemoteDataSource
import com.fathan.e_commerce.data.repository.AuthRepositoryImpl
import com.fathan.e_commerce.data.repository.FavoriteRepositoryImpl
import com.fathan.e_commerce.features.product.data.repository.ProductRepositoryImpl
import com.fathan.e_commerce.data.repository.PromoRepositoryImpl
import com.fathan.e_commerce.data.repository.WishlistRepositoryImpl
import com.fathan.e_commerce.domain.repository.AuthRepository
import com.fathan.e_commerce.domain.repository.FavoriteRepository
import com.fathan.e_commerce.features.product.domain.repository.ProductRepository
import com.fathan.e_commerce.domain.repository.PromoRepository
import com.fathan.e_commerce.domain.repository.WishlistRepository
import com.fathan.e_commerce.domain.usecase.auth.LogoutUseCase
import com.fathan.e_commerce.features.chat.domain.usecase.ChatUseCases
import com.fathan.e_commerce.features.chat.domain.usecase.DeleteChat
import com.fathan.e_commerce.features.chat.domain.usecase.FetchAllChats
import com.fathan.e_commerce.features.chat.domain.usecase.FetchChatByRoom
import com.fathan.e_commerce.features.chat.domain.usecase.FindChat
import com.fathan.e_commerce.features.chat.domain.usecase.MarkAllAsRead
import com.fathan.e_commerce.features.chat.domain.usecase.ReadByRoom
import com.fathan.e_commerce.features.chat.domain.usecase.SendAudio
import com.fathan.e_commerce.features.chat.domain.usecase.SendImage
import com.fathan.e_commerce.features.chat.domain.usecase.SendText
import com.fathan.e_commerce.features.product.domain.usecase.GetCategoriesUseCase
import com.fathan.e_commerce.features.product.domain.usecase.GetFavoritesUseCase
import com.fathan.e_commerce.features.product.domain.usecase.GetFlashSaleUseCase
import com.fathan.e_commerce.features.product.domain.usecase.GetProductDetailUseCase
import com.fathan.e_commerce.features.product.domain.usecase.GetProductsUseCase
import com.fathan.e_commerce.features.product.domain.usecase.GetProductsUseCaseV2
import com.fathan.e_commerce.features.product.domain.usecase.IsFavoriteUseCase
import com.fathan.e_commerce.features.product.domain.usecase.SearchProductsUseCase
import com.fathan.e_commerce.features.product.domain.usecase.ToggleFavoriteUseCase
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

//    @RequiresApi(Build.VERSION_CODES.O)
//    @Provides
//    @Singleton
//    fun provideMessageRepository(
//        dao: FavoriteDao
//    ): ChatRepository = ChatRepositoryImpl()

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

//
//    @Provides
//    @Singleton
//    fun provideGetChatsUseCase(
//        repo: ChatRepository
//    ): GetMessagesUseCase = GetMessagesUseCase(repo)
//

//    @Provides
//    @Singleton
//    fun provideSendChatUseCase(
//        repo: ChatRepository
//    ): SendTextMessageUseCase = SendTextMessageUseCase(repo)
//
//    @Provides
//    @Singleton
//    fun provideSendImageChatUseCase(
//        repo: ChatRepository
//    ): SendImageMessageUseCase = SendImageMessageUseCase(repo)
//
//    @Provides
//    @Singleton
//    fun provideSendAudioChatUseCase(
//        repo: ChatRepository
//    ): SendAudioMessageUseCase = SendAudioMessageUseCase(repo)

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
    fun provideProductDetailUseCase(
        productRepository: ProductRepository
    ): GetProductDetailUseCase = GetProductDetailUseCase(productRepository)

    @Provides
    @Singleton
    fun provideLogoutUseCase(
        authRepository: AuthRepository
    ): LogoutUseCase = LogoutUseCase(authRepository)


    @Provides    @Singleton
    fun provideWishlistRepository(wishlistDao: WishlistDao): WishlistRepository {
        return WishlistRepositoryImpl(wishlistDao)
    }


    @Provides
    @Singleton
    fun provideUseCases(repo: com.fathan.e_commerce.features.chat.domain.repository.ChatRepository): ChatUseCases {
        return ChatUseCases(
            fetchAllChats = FetchAllChats(repo),
            fetchChatByRoom = FetchChatByRoom(repo),
            readByRoom = ReadByRoom(repo),
            markAllAsRead = MarkAllAsRead(repo),
            deleteChat = DeleteChat(repo),
            findChat = FindChat(repo),
            sendText = SendText(repo),
            sendImage = SendImage(repo),
            sendAudio = SendAudio(repo),
            incomingMessages = repo.incomingMessages
        )
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
