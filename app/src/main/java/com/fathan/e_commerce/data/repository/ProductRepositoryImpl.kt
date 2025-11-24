package com.fathan.e_commerce.data.repository

import com.fathan.e_commerce.domain.model.DummyData
import com.fathan.e_commerce.domain.model.Product
import com.fathan.e_commerce.domain.repository.ProductRepository

class ProductRepositoryImpl : ProductRepository {
    override suspend fun getProducts(): List<Product> {
        return DummyData.products
    }
}