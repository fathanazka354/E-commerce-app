package com.fathan.e_commerce.domain.repository

import com.fathan.e_commerce.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(): List<Product>
}