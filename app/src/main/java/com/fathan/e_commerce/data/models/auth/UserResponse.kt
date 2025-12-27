package com.fathan.e_commerce.data.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val avatar: String? = null,
    val auth_id: String? = null,
)
