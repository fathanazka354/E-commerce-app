package com.fathan.e_commerce.data.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val id: String,
    val email: String
)


@Serializable
data class IdOnly(val id: Long)
