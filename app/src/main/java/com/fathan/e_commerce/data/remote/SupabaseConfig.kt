package com.fathan.e_commerce.data.remote

import com.fathan.e_commerce.BuildConfig

object SupabaseConfig {
    // Replace with your Supabase values
    const val SUPABASE_URL = BuildConfig.SUPABASE_URL // no trailing slash
    const val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY
    const val STORAGE_BUCKET = "chat-media"

    // Realtime endpoint
    // wss://<project>.supabase.co/realtime/v1?apikey=<anon-key>&vsn=1.0.0
    fun realtimeUrl(anonKey: String = SUPABASE_ANON_KEY) =
        "$SUPABASE_URL/realtime/v1?apikey=$anonKey&vsn=1.0.0"

    // REST base
    fun restUrl() = "$SUPABASE_URL/rest/v1"
    // Storage upload and public url helpers
    fun storageUrl() = "$SUPABASE_URL/storage/v1"
    fun storagePublicUrl(bucket: String, path: String) =
        "$SUPABASE_URL/storage/v1/object/public/$bucket/$path"
}
