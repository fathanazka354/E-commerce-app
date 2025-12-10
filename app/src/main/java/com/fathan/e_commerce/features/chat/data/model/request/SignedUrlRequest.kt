package com.fathan.e_commerce.features.chat.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class SignedUrlRequest(val expiresIn: Int)