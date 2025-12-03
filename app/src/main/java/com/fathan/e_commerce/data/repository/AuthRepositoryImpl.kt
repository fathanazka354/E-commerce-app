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
import java.security.MessageDigest
import javax.inject.Inject

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
                saveUserToSupabase(user.id, email, password)

                AuthResult.Success(
                    AuthUser(
                        uid = user.id,
                        email = user.email ?: ""
                    )
                )
            } else {
                AuthResult.Error("Login Failed: User not found")
            }
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
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

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Save user data to Supabase table "users"
     * Only saves if user with same email doesn't exist
     *
     * NOTE: Sebaiknya **jangan** simpan password plain text di table ini.
     * Lebih aman pakai kolom lain (display name, avatar, dsb.) dan biarkan Auth yang handle password.
     */
    private suspend fun saveUserToSupabase(uid: String, email: String, password: String) {
        try {
            // Cek apakah user sudah ada berdasarkan email
            val existingUser = postgrest.from("users")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeSingleOrNull<SupabaseUser>()

            if (existingUser == null) {
                Log.d("AUTH_REPOSITORY_IMPL", "saveUserToSupabase: SAVED")

                val name = email.substringBefore("@")

                val userData = SupabaseUser(
                    id = uid,
                    name = name,
                    email = email,
                    password = password // ❗ tidak disarankan di production
                )

                postgrest.from("users").insert(userData)
            } else {
                Log.d("AUTH_REPOSITORY_IMPL", "saveUserToSupabase: User already exists, skipping")
            }
        } catch (e: Exception) {
            Log.e("AUTH_REPOSITORY_IMPL", "Error saving user to Supabase", e)
            e.printStackTrace()
        }
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

    override suspend fun logout() {
        supabaseAuth.signOut()
    }
}
