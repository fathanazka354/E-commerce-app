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
            Log.d("AuthRepositoryIMPL", "login: Starting login for $email")

            // 1) Login to Supabase Auth with EMAIL + PASSWORD
            // ❌ WRONG: supabaseAuth.signInWith(Email) - this sends magic link
            // ✅ CORRECT: Use signInWith(Email) with password
            supabaseAuth.signInWith(Email) {
                this.email = email.trim()
                this.password = password  // ← THIS WAS MISSING!
            }

            Log.d("AuthRepositoryIMPL", "login: Auth successful")

            // 2) Get session after login
            val session = supabaseAuth.currentSessionOrNull()
            val user = session?.user

            Log.d("AuthRepositoryIMPL", "login: Got session")

            if (user != null) {
                Log.d("AuthRepositoryIMPL", "login: User found in auth")

                // 3) Verify user exists in "users" table
                val hashedInput = hashPassword(password)

                val existingUser = postgrest.from("users")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("email", email.trim())
                            eq("password", hashedInput)
                        }
                    }
                    .decodeSingleOrNull<SupabaseUser>()

                Log.d("AuthRepositoryIMPL", "login: Checked users table")

                if (existingUser == null) {
                    Log.e("AuthRepositoryIMPL", "login: User not found in users table")
                    return AuthResult.Error("Login Failed: User not found")
                }

                Log.d("AuthRepositoryIMPL", "login: Login successful")

                AuthResult.Success(
                    AuthUser(
                        uid = user.id,
                        email = user.email ?: "",
                    )
                )
            } else {
                Log.e("AuthRepositoryIMPL", "login: No user in session")
                AuthResult.Error("Login Failed: User not found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val msg = e.message.orEmpty()
            Log.e("AuthRepositoryIMPL", "login: Exception - $msg")

            val errorMessage = when {
                msg.contains("Invalid login credentials", ignoreCase = true) ->
                    "Email atau password salah"
                msg.contains("Email not confirmed", ignoreCase = true) ->
                    "Email belum dikonfirmasi"
                msg.contains("User not found", ignoreCase = true) ->
                    "User tidak ditemukan"
                msg.contains("Network", ignoreCase = true) ->
                    "Koneksi internet bermasalah"
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
        accessToken: String, // This should be access_token from URL
        newPassword: String
    ): AuthResult<Boolean> {
        return try {
            Log.d("ResetPassword", "Step 1: Getting user info from access token")

            // STEP 1: Get user info using the access token from URL
            val sessionResult = remoteDataSource.verifyRecoveryTokenFromURL(accessToken)
            if (sessionResult.isFailure) {
                return AuthResult.Error(
                    sessionResult.exceptionOrNull()?.message ?: "Failed to verify token"
                )
            }

            val sessionInfo = sessionResult.getOrNull()!!
            val email = sessionInfo.email

            Log.d("ResetPassword", "Step 2: Token verified for email: $email")

            if (email.isNullOrBlank()) {
                return AuthResult.Error("Email not found in token")
            }

            // STEP 2: Update password in Supabase Auth using the access token
            val authUpdate = remoteDataSource.updatePasswordWithAccessToken(accessToken, newPassword)
            if (authUpdate.isFailure) {
                return AuthResult.Error(
                    authUpdate.exceptionOrNull()?.message ?: "Failed to update auth password"
                )
            }

            Log.d("ResetPassword", "Step 3: Auth password updated")

            // STEP 3: Hash password for local storage
            val hashedPassword = hashPassword(newPassword)

            // STEP 4: Check if user exists in users table
            val existsResult = remoteDataSource.findUserByEmail(email)
            if (existsResult.isFailure) {
                return AuthResult.Error(
                    "Failed checking user: ${existsResult.exceptionOrNull()?.message}"
                )
            }

            val exists = existsResult.getOrNull() == true

            Log.d("ResetPassword", "Step 4: User exists in table: $exists")

            if (exists) {
                // STEP 5: Update password in users table
                val updateResult = remoteDataSource.updateUsersTablePassword(
                    email = email,
                    hashedPassword = hashedPassword
                )

                if (updateResult.isFailure) {
                    return AuthResult.Error(
                        updateResult.exceptionOrNull()?.message ?: "Failed to update users table"
                    )
                }

                Log.d("ResetPassword", "Step 5: Users table updated")
            }

            Log.d("ResetPassword", "Password reset completed successfully!")
            AuthResult.Success(true)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ResetPassword", "Error: ${e.message}")
            AuthResult.Error(e.message ?: "Password reset failed")
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