package com.fathan.e_commerce.data.models
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Long,
    val name: String? = null,
    val icon: String? = null
)
