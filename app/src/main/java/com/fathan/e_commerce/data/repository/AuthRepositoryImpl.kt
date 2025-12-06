package com.fathan.e_commerce.data.repository

import android.util.Log
import com.fathan.e_commerce.data.models.SupabaseUser
import com.fathan.e_commerce.data.models.auth.SignUpResult
import com.fathan.e_commerce.data.remote.SupabaseUserRemoteDataSource
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

    override suspend fun login(email: String, password: String): AuthResult<AuthUser> {
        return try {
            // 1) Login ke Supabase Auth
            supabaseAuth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            // 2) Ambil session setelah login
            val session = supabaseAuth.currentSessionOrNull()
            val user = session?.user


            if (user != null) {
                // 3) Simpan user di table "users" kalau belum ada
                val hashedInput = hashPassword(password)

                val existingUser = postgrest.from("users")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("email", email)
                            eq("password", hashedInput)
                        }
                    }
                    .decodeSingleOrNull<SupabaseUser>()
                Log.d("TAG", "login: User not found 1")

                if (existingUser == null) return AuthResult.Error("Login Failed: User not found")

                AuthResult.Success(
                    AuthUser(
                        uid = user.id,
                        email = user.email ?: ""
                    )
                )
            } else {
                Log.d("TAG", "login: User not found 2")
                AuthResult.Error("Login Failed: User not found")
            }
        } catch (e: Exception) {

            val msg = e.message.orEmpty()
            Log.d("TAG", "${msg}")
            val errorMessage = when {
                msg.contains("Invalid login credentials", ignoreCase = true) ->
                    "Email atau password salah"
                msg.contains("Email not confirmed", ignoreCase = true) ->
                    "Email belum dikonfirmasi"
                msg.contains("User not found", ignoreCase = true) ->
                    "User tidak ditemukan"
                else -> msg.ifBlank { "Login gagal. Silakan coba lagi." }
            }
            AuthResult.Error(errorMessage)
        }
    }

    override suspend fun signUp(params: SignUpParams): SignUpResult {
        return try {
            val authResponse = supabaseAuth.signUpWith(Email){
                email = params.email
                password = params.password
            }

            if (authResponse == null) return SignUpResult.Error("Sign Up Failed")

            val hashedPassword = hashPassword(params.password)
            val userId = remoteDataSource.createUserWithRelations(
                name = params.name,
                email = params.email,
                hashedPassword = hashedPassword,
                accountType = params.accountType
            )
            SignUpResult.Success(userId)
        } catch (e: Exception){
            e.printStackTrace()
            SignUpResult.Error(e.message ?: "Sign Up Failed")
        }
    }
    override suspend fun resetPasswordWithToken(
        token: String,
        newPassword: String
    ): AuthResult<Boolean> {

        return try {

            // 1. Update password Auth
            val authUpdate = remoteDataSource.updatePasswordWithToken(token, newPassword)
            if (authUpdate.isFailure) {
                return AuthResult.Error(authUpdate.exceptionOrNull()?.message ?: "Failed update auth password")
            }

            // 2. Ambil email user dari token
            val userInfoResult = remoteDataSource.getUserFromToken(token)
            if (userInfoResult.isFailure) {
                return AuthResult.Error(userInfoResult.exceptionOrNull()?.message ?: "Failed get user from token")
            }
            val userInfo = userInfoResult.getOrNull()!!
            val email = userInfo.email ?: return AuthResult.Error("Email not found in token")

            // 3. Hash password
            val hashedPassword = hashPassword(newPassword)

            // 4. Cek apakah user ada di table users
            val existsResult = remoteDataSource.findUserByEmail(email)
            if (existsResult.isFailure) {
                return AuthResult.Error("Failed checking user exist: ${existsResult.exceptionOrNull()?.message}")
            }

            val exists = existsResult.getOrNull() == true

            if (exists) {
                // 5. Update password saja
                val updateResult = remoteDataSource.updateUsersTablePassword(
                    email = email,
                    hashedPassword = hashedPassword
                )

                if (updateResult.isFailure) {
                    return AuthResult.Error(updateResult.exceptionOrNull()?.message ?: "Failed update users table")
                }
            }

            AuthResult.Success(true)

        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.Error(e.message ?: "Update Password Failed")
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
