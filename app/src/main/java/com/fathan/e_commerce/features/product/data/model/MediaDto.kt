package com.fathan.e_commerce.features.product.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaDto(
    val id: Int,
    val path_url: String? = null,
    val type: Int? = 1
)