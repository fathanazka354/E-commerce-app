package com.fathan.e_commerce.di

import com.fathan.e_commerce.BuildConfig
import com.fathan.e_commerce.data.remote.api.RetrofitProvider
import com.fathan.e_commerce.data.remote.api.SupabaseApi
import com.fathan.e_commerce.features.chat.data.api.SupabaseChatApi
import com.fathan.e_commerce.features.chat.data.repository.ChatRepositoryImpl
import com.fathan.e_commerce.features.chat.data.source.SupabaseRemoteDataSource
import com.fathan.e_commerce.features.chat.domain.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY

    @Provides
    @Singleton
    fun provideSupabaseApi(): SupabaseApi = RetrofitProvider.create(SUPABASE_URL)

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth = client.auth

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient): Postgrest = client.postgrest

    @Provides
    @Singleton
    @Named("chat_retrofit")
    fun provideChatRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        val json = Json { ignoreUnknownKeys = true }
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(SUPABASE_URL)
//            .baseUrl("http://10.0.2.2:54321")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideSupabaseChatApi(@Named("chat_retrofit") retrofit: Retrofit): SupabaseChatApi {
        return retrofit.create(SupabaseChatApi::class.java)
    }



//    @Provides
//    @Singleton
//    fun provideAppCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideSupabaseRemoteDataSource(
        api: SupabaseChatApi,
        postgrest: Postgrest,
        supabaseClient: SupabaseClient
    ): SupabaseRemoteDataSource {
        return SupabaseRemoteDataSource(api = api, supabaseUrl = SUPABASE_URL, anonKey = SUPABASE_ANON_KEY, postgrest = postgrest, userJwt = "", supabaseClient = supabaseClient)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        remote: SupabaseRemoteDataSource,
        scope: CoroutineScope,
        supabaseClient: SupabaseClient
    ): ChatRepository {
        return ChatRepositoryImpl(remote = remote, scope = scope,  supabase =  supabaseClient)
    }

}
