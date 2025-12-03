package com.fathan.e_commerce.data.remote

import com.fathan.e_commerce.data.models.auth.CreateUserWithRelationsParams
import com.fathan.e_commerce.domain.entities.auth.AccountType
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject

class SupabaseUserRemoteDataSource @Inject constructor(
    private val postgrest: Postgrest
) {


    suspend fun createUserWithRelations(
        name: String,
        email: String,
        hashedPassword: String,
        accountType: AccountType
    ): Long {
        val roleId = when (accountType) {
            AccountType.SELLER -> 2L
            AccountType.BUYER -> 3L
        }

        val params = CreateUserWithRelationsParams(
            name = name,
            email = email,
            password = hashedPassword,
            roleId = roleId
        )

        val result = postgrest.rpc(
            function = "create_user_with_relations",
            parameters = params
        )

        val userId: Long = result.decodeAs()

        return userId
    }
}