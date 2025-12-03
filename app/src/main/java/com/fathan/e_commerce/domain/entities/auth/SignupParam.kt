package com.fathan.e_commerce.domain.entities.auth

data class SignUpParams(
    val name: String,
    val email: String,
    val password: String,      // plain, nanti di-hash di repo
    val accountType: AccountType
)
