package com.fathan.e_commerce.data.remote

import com.fathan.e_commerce.BuildConfig
import com.fathan.e_commerce.data.models.RecoverRequest
import com.fathan.e_commerce.data.models.auth.CreateUserWithRelationsParams
import com.fathan.e_commerce.data.models.auth.IdOnly
import com.fathan.e_commerce.data.models.auth.UserInfo
import com.fathan.e_commerce.domain.entities.auth.AccountType
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

    private val client = HttpClient(Android)

    suspend fun sendPasswordRecoveryEmail(email: String, redirectTo: String? = null): Result<Unit> {
        val url = "${BuildConfig.SUPABASE_URL}/auth/v1/recover"
        val redir = BuildConfig.MY_DEPLOYED_URL

        val body = RecoverRequest(email = email, redirect_to = redir)
        return try {
            val resp: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Accept, "application/json")
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                setBody(Json.encodeToString(body))
            }
            if (resp.status.value in 200..299) Result.success(Unit)
            else Result.failure(Exception("Recover failed: ${resp.status.value}: ${resp.bodyAsText()}"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun getUserFromToken(accessToken: String): Result<UserInfo> {
        val url = "${BuildConfig.SUPABASE_URL}/auth/v1/user"
        return try {
            val resp: HttpResponse = client.get(url) {
                header(HttpHeaders.Accept, "application/json")
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            if (resp.status.value in 200..299) {
                val body = resp.bodyAsText()
                val json = Json.parseToJsonElement(body).jsonObject
                val id = json["id"]?.jsonPrimitive?.content
                val email = json["email"]?.jsonPrimitive?.content
                if (!id.isNullOrBlank() && !email.isNullOrBlank()) {
                    Result.success(UserInfo(
                        id = id, email = email,
                    ))
                } else {
                    Result.failure(Exception("Cannot parse user from token response: $body"))
                }
            } else {
                Result.failure(Exception("Get user failed: ${resp.status.value}: ${resp.bodyAsText()}"))
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun findUserByEmail(email: String): Result<Boolean> {
        return try {
            val list: List<IdOnly> = postgrest
                .from("users")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList()

            Result.success(list.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun updateUsersTablePassword(
        email: String,
        hashedPassword: String
    ): Result<Unit> {
        return try {
            postgrest
                .from("users")
                .update(
                    {
                        set("password", hashedPassword)
                    }
                ) {
                    filter {
                        eq("email", email)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun updatePasswordWithToken(accessToken: String, newPassword: String): Result<Unit> {
        val url = "${BuildConfig.SUPABASE_URL}/auth/v1/user"
        return try {
            val resp: HttpResponse = client.put(url) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Accept, "application/json")
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                setBody(Json.encodeToString(mapOf("password" to newPassword)))
            }
            if (resp.status.value in 200..299) Result.success(Unit)
            else Result.failure(Exception("Update password failed: ${resp.status.value}: ${resp.bodyAsText()}"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}