package com.fathan.e_commerce.data.repository

import android.util.Log
import com.fathan.e_commerce.data.models.SupabaseUser
import com.fathan.e_commerce.data.models.auth.SignUpResult
import com.fathan.e_commerce.data.remote.SupabaseUserRemoteDataSource
import com.fathan.e_commerce.domain.entities.auth.AccountType
import com.fathan.e_commerce.domain.entities.auth.SignUpParams
import com.fathan.e_commerce.domain.model.AuthUser
import com.fathan.e_commerce.domain.repository.AuthRepository
import com.fathan.e_commerce.domain.repository.AuthResult
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AuthRepositoryImpl @Inject constructor(
    private val supabaseAuth: Auth,
    private val remoteDataSource: SupabaseUserRemoteDataSource,
    private val postgrest: Postgrest
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String
    ): AuthResult<AuthUser> {
        return try {
            Log.d("AuthRepositoryImpl", "wokowk: ${email} | $password")
            supabaseAuth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
            Log.d("AuthRepositoryImpl", "wokowk: 2")


            val session = supabaseAuth.currentSessionOrNull()
                ?: return AuthResult.Error("Session not found")
            Log.d("AuthRepositoryImpl", "login: ${session}")

            val user = session.user

            AuthResult.Success(
                AuthUser(
                    uid = user?.id?:"",
                    email = user?.email.orEmpty()
                )
            )
        } catch (e: Exception) {
            Log.d("AuthRepositoryImpl", "error: ${e.message}")
            AuthResult.Error(
                when {
                    e.message?.contains("Invalid login credentials", true) == true ->
                        "Email atau password salah"
                    e.message?.contains("Email not confirmed", true) == true ->
                        "Email belum dikonfirmasi"
                    else -> e.message ?: "Login gagal"
                }
            )
        }
    }


    override suspend fun signUp(params: SignUpParams): SignUpResult {
        return try {
            // 1️⃣ Sign up ke Supabase Auth
            val authResult = supabaseAuth.signUpWith(Email) {
                email = params.email
                password = params.password
            } ?: return SignUpResult.Error("Signup failed")

            val roleId = when (params.accountType) {
                AccountType.SELLER -> 2L
                AccountType.BUYER -> 3L
            }

            // 2️⃣ Insert profile ke users table
            val userId = remoteDataSource.createUserWithRelations(
                name = params.name,
                email = params.email,
                roleId = roleId,

            )

            SignUpResult.Success(userId)
        } catch (e: Exception) {
            SignUpResult.Error(e.message ?: "Signup error")
        }
    }

    override suspend fun resetPasswordWithToken(
        accessToken: String,
        newPassword: String
    ): AuthResult<Boolean> {
        return try {
            // Cukup update ke Supabase Auth
            remoteDataSource.updatePasswordWithAccessToken(
                accessToken,
                newPassword
            )

            AuthResult.Success(true)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Reset password gagal")
        }
    }

    override suspend fun requestPasswordReset(
        email: String,
        redirectTo: String?
    ): AuthResult<Boolean> {

        return try {
            remoteDataSource.sendPasswordRecoveryEmail(email, redirectTo)
            AuthResult.Success(
                data = true
            )
        } catch (e: Exception){
            e.printStackTrace()
            AuthResult.Error(e.message ?: "Update Password Failed")
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }



//    override fun currentUser(): AuthUser? {
//        val session = supabaseAuth.currentSessionOrNull()
//        val user = session?.user ?: return null
//
//        return AuthUser(
//            uid = user.id,
//            email = user.email ?: ""
//        )
//    }

    override suspend fun logout(): Boolean {
        return try {
            withContext(NonCancellable) {
                try {
                    supabaseAuth.signOut()
                    Log.d("AuthRepository", "Logout successful")
                    true
                } catch (e: CancellationException) {
                    Log.d("AuthRepository", "Logout cancelled but considered successful")
                    true
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Logout failed", e)
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Logout error", e)
            false
        }
    }
}