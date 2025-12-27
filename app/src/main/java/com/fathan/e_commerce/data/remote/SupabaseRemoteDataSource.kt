package com.fathan.e_commerce.data.remote

import android.util.Log
import com.fathan.e_commerce.BuildConfig
import com.fathan.e_commerce.data.models.auth.IdOnly
import com.fathan.e_commerce.data.remote.api.RecoverRequest
import com.fathan.e_commerce.data.remote.api.SupabaseApi
import com.fathan.e_commerce.data.remote.api.UpdatePasswordRequest
import com.fathan.e_commerce.data.remote.api.UserResponse
import com.fathan.e_commerce.data.models.auth.CreateUserWithRelationsParams
import com.fathan.e_commerce.domain.entities.auth.AccountType
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject

interface UserRemoteDataSource {
    suspend fun createUserWithRelations(
        name: String,
        email: String,
        roleId: Long
    ): String

    suspend fun sendPasswordRecoveryEmail(email: String, redirectTo: String? = null): Result<Unit>

    suspend fun verifyRecoveryTokenFromURL(accessToken: String): Result<SessionInfo>

    suspend fun updatePasswordWithAccessToken(accessToken: String, newPassword: String): Result<Unit>

    suspend fun findUserByEmail(email: String): Result<Boolean>

    suspend fun updateUsersTablePassword(email: String, hashedPassword: String): Result<Unit>
}

class SupabaseUserRemoteDataSource @Inject constructor(
    private val postgrest: Postgrest,
    private val api: SupabaseApi
) : UserRemoteDataSource {

    override suspend fun createUserWithRelations(
        name: String,
        email: String,
        roleId: Long
    ): String {
        val params = mapOf(
            "p_name" to name,
            "p_email" to email,
            "p_role_id" to roleId
        )

        val result = postgrest.rpc(
            function = "create_user_with_relations",
            parameters = params
        )

        return result.decodeAs()
    }

    private val anonKeyHeader = BuildConfig.SUPABASE_ANON_KEY
    private val apiKeyHeader = BuildConfig.SUPABASE_ANON_KEY
    private val baseRedirect = BuildConfig.MY_DEPLOYED_URL

    override suspend fun sendPasswordRecoveryEmail(email: String, redirectTo: String?): Result<Unit> {
        return try {
            val body = RecoverRequest(email = email, redirect_to = redirectTo ?: baseRedirect)
            val resp = api.sendRecoverEmail(
                apiKeyHeader,
                "Bearer $anonKeyHeader",
                body
            )

            if (resp.isSuccessful) Result.success(Unit)
            else {
                val err = "Recover failed: ${resp.code()} ${resp.errorBody()?.string()}"
                Log.e(TAG, err)
                Result.failure(Exception(err))
            }
        } catch (t: Throwable) {
            Log.e(TAG, "sendPasswordRecoveryEmail error", t)
            Result.failure(t)
        }
    }

    override suspend fun verifyRecoveryTokenFromURL(accessToken: String): Result<SessionInfo> {
        return try {
            Log.d(TAG, "verifyRecoveryTokenFromURL")
            val resp = api.getUser(apiKeyHeader, "Bearer $accessToken")

            if (!resp.isSuccessful) {
                val err = "Get user failed: ${resp.code()} ${resp.errorBody()?.string()}"
                Log.e(TAG, err)
                return Result.failure(Exception(err))
            }

            val body: UserResponse? = resp.body()
            if (body == null || body.id.isBlank()) {
                return Result.failure(Exception("Invalid user response"))
            }

            Result.success(
                SessionInfo(
                    userId = body.id,
                    email = body.email,
                    accessToken = accessToken,
                    refreshToken = null
                )
            )
        } catch (t: Throwable) {
            Log.e(TAG, "verifyRecoveryTokenFromURL error", t)
            Result.failure(t)
        }
    }
    override suspend fun updatePasswordWithAccessToken(
        accessToken: String,
        newPassword: String
    ): Result<Unit> {
        val body = UpdatePasswordRequest(password = newPassword)
        val resp = api.updateUserPassword(
            BuildConfig.SUPABASE_ANON_KEY,
            "Bearer $accessToken",
            body
        )
        return if (resp.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Update password failed"))
    }

    override suspend fun findUserByEmail(email: String): Result<Boolean> {
        return try {
            val list: List<IdOnly> = postgrest
                .from("users")
                .select(columns = Columns.list("id")) {
                    filter { eq("email", email) }
                }
                .decodeList()

            Result.success(list.isNotEmpty())
        } catch (e: Exception) {
            Log.e(TAG, "findUserByEmail error", e)
            Result.failure(e)
        }
    }

    override suspend fun updateUsersTablePassword(email: String, hashedPassword: String): Result<Unit> {
        return try {
            postgrest
                .from("users")
                .update({
                    set("password", hashedPassword)
                }) {
                    filter { eq("email", email) }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateUsersTablePassword error", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "SupabaseUserRemoteDS"
    }
}

/** Session DTO kept inside same file for convenience **/
data class SessionInfo(
    val userId: String,
    val email: String?,
    val accessToken: String,
    val refreshToken: String?
)
