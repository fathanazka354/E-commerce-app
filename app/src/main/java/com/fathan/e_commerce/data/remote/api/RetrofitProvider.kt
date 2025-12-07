package com.fathan.e_commerce.data.remote.api

import com.fathan.e_commerce.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitProvider {

    private val json = Json { ignoreUnknownKeys = true }

    fun create(baseUrl: String, enableLogging: Boolean = BuildConfig.DEBUG): SupabaseApi {
        val logging = HttpLoggingInterceptor().apply {
            level = if (enableLogging) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory(contentType))
            .client(client)
            .build()

        return retrofit.create(SupabaseApi::class.java)
    }
}
