package com.fathan.e_commerce.data.models.auth
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CreateUserWithRelationsParams(
    @SerialName("p_name") val name: String,
    @SerialName("p_email") val email: String,
    @SerialName("p_password") val password: String,
    @SerialName("p_role_id") val roleId: Long
)