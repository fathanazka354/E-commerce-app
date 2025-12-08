package com.fathan.e_commerce.data.models

import kotlinx.serialization.Serializable
// Untuk response product.rate (hanya satu kolom rate)
@Serializable
data class RateOnlyDto(
    val rate: Double? = null
)

// Untuk response feedback rows (kita hanya butuh id atau rating/description)
@Serializable
data class FeedbackSimpleDto(
    val id: Int,
    val description: String? = null
)