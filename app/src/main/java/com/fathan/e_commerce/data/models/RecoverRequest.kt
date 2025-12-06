package com.fathan.e_commerce.data.models

import kotlinx.serialization.Serializable

@Serializable
data class RecoverRequest(val email: String, val redirect_to: String? = null)
