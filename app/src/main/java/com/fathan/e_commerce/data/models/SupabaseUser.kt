package com.fathan.e_commerce.data.models

import kotlinx.serialization.Serializable

/** Data class for Supabase users table This matches the structure in your Supabase database */
@Serializable
data class SupabaseUser(
        val id: Long,
        val name: String,
        val email: String,
        val password: String? = null // Optional, for security reasons
)