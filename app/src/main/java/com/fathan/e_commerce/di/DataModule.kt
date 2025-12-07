package com.fathan.e_commerce.di

import com.fathan.e_commerce.data.remote.ProductRemoteDataSource
import com.fathan.e_commerce.data.remote.ProductRemoteDataSourceImpl
import com.fathan.e_commerce.data.repository.ProductRepositoryImpl
import com.fathan.e_commerce.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindProductRemoteDataSource(
        impl: ProductRemoteDataSourceImpl
    ): ProductRemoteDataSource

}
