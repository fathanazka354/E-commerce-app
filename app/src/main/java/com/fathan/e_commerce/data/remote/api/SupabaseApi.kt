package com.fathan.e_commerce.data.remote.api

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Retrofit interface for Supabase Auth endpoints used in this module.
 */
interface SupabaseApi {

    @POST("/auth/v1/recover")
    suspend fun sendRecoverEmail(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body body: RecoverRequest
    ): Response<Unit>

    @GET("/auth/v1/user")
    suspend fun getUser(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String
    ): Response<UserResponse>

    @PUT("/auth/v1/user")
    suspend fun updateUserPassword(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body body: UpdatePasswordRequest
    ): Response<Unit>
}

/** DTOs used by Retrofit endpoints **/
@Serializable
data class RecoverRequest(val email: String, val redirect_to: String? = null)

@Serializable
data class UpdatePasswordRequest(val password: String)

@Serializable
data class UserResponse(
    val id: String,
    val email: String? = null,
    // add other fields if needed
)
