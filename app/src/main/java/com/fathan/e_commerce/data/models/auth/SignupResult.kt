package com.fathan.e_commerce.data.models.auth
sealed class SignUpResult {
    data class Success(val userId: Long) : SignUpResult()
    data class Error(val message: String) : SignUpResult()
}