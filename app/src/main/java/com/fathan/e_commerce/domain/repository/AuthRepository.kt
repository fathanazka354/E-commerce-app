package com.fathan.e_commerce.domain.repository

import com.fathan.e_commerce.data.models.auth.SignUpResult
import com.fathan.e_commerce.domain.entities.auth.SignUpParams
import com.fathan.e_commerce.domain.model.AuthUser

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult<AuthUser>
    suspend fun signUp(params: SignUpParams): SignUpResult
    suspend fun resetPasswordWithToken(token: String, newPassword: String): AuthResult<Boolean>
    suspend fun requestPasswordReset(email: String, redirectTo: String?): AuthResult<Boolean>

    suspend fun logout(): Boolean
}