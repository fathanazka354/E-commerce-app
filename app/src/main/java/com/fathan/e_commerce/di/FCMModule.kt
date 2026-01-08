package com.fathan.e_commerce.di

import com.fathan.e_commerce.data.repository.FCMTokenRepository
import com.fathan.e_commerce.data.repository.FCMTokenRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FCMModule {

    @Binds
    @Singleton
    abstract fun bindFCMTokenRepository(
        impl: FCMTokenRepositoryImpl
    ): FCMTokenRepository
}