package com.fathan.e_commerce.domain.repository

import com.fathan.e_commerce.domain.model.AuthUser

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult<AuthUser>
    fun currentUser(): AuthUser?
    suspend fun logout()
}