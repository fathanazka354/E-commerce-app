package com.fathan.e_commerce.features.chat.data.source

import android.util.Log
import com.fathan.e_commerce.features.chat.data.api.SupabaseChatApi
import com.fathan.e_commerce.features.chat.data.model.request.InsertMessageRequest
import com.fathan.e_commerce.features.chat.data.model.request.SignedUrlRequest
import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * SupabaseRemoteDataSource using supabase-kt (Postgrest client) for DB ops and
 * Retrofit (SupabaseChatApi) for storage endpoints (upload / signed url).
 *
 * IMPORTANT:
 * - Make sure you have dependency for supabase-kt / postgrest-kt in your Gradle.
 * - The Postgrest client (io.github.jan.supabase.postgrest.Postgrest) must be
 *   created/configured in DI and injected here.
 */
class SupabaseRemoteDataSource(
    private val api: SupabaseChatApi,
    private val postgrest: Postgrest,
    private val supabaseUrl: String,
    private val anonKey: String,
    private var userJwt: String // must be kept up-to-date with logged-in user's JWT
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.IO)

    // incoming realtime messages mapped to MessageResponse
    private val _incomingMessagesRaw = MutableSharedFlow<MessageResponse>(replay = 50)
    val incomingMessagesRaw = _incomingMessagesRaw.asSharedFlow()

    // OkHttp client for WebSocket (kept for realtime fallback)
    private val okClient: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null

    // ------------------ PostgREST (DB) wrapper ------------------

    /**
     * Insert a message row and return representation (MessageResponse).
     * Uses Postgrest client so it respects RLS and uses configured client auth.
     */
    suspend fun insertMessage(payload: InsertMessageRequest): MessageResponse {
        // insert and return representation (use select() inside insert builder)
        val inserted = postgrest.from("messages")
            .insert(payload) {
                // return the inserted row(s)
                select()
            }
            .decodeSingle<MessageResponse>()

        Log.d("SupabaseRemoteDataSource", "insertMessage -> ${inserted.id}")
        return inserted
    }

    /**
     * Fetch messages by room id (ascending by created_at).
     */
    suspend fun fetchMessagesByRoom(roomId: String, limit: Int = 200): List<MessageResponse> {
        val list = postgrest.from("messages")
            .select {
                filter {
                    eq("room_id", roomId)
                }
                order("created_at", order = Order.ASCENDING)
                limit(limit.toLong())
            }
            .decodeList<MessageResponse>()

        Log.d("SupabaseRemoteDataSource", "fetchMessagesByRoom($roomId) -> ${list.size} items")
        return list
    }

    /**
     * Fetch all messages (descending by created_at).
     * You might want to replace this with an RPC that returns latest-per-room to optimize.
     */
    suspend fun fetchAllMessages(limit: Int = 1000): List<MessageResponse> {
        val list = postgrest.from("messages")
            .select {
                order("created_at", order = Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<MessageResponse>()

        Log.d("SupabaseRemoteDataSource", "fetchAllMessages -> ${list.size} items")
        return list
    }

    /**
     * Mark messages in a room as read (set is_read = true) and return modified rows.
     */
    suspend fun markReadByRoom(roomId: String): List<MessageResponse> {
        val updated = postgrest.from("messages")
            .update(
                mapOf("is_read" to true)
            ) {
                filter {
                    eq("room_id", roomId)
                }
                select()
            }
            .decodeList<MessageResponse>()

        Log.d("SupabaseRemoteDataSource", "markReadByRoom($roomId) -> updated ${updated.size}")
        return updated
    }

    /**
     * Mark all messages as read for a given receiver (useful if you store receiver_id).
     * If you rely on RLS with auth.uid(), call this with receiverId = auth.uid() from server-side or use client auth.
     */
    suspend fun markAllAsReadForReceiver(receiverId: String): List<MessageResponse> {
        val updated = postgrest.from("messages")
            .update(
                mapOf("is_read" to true)
            ) {
                filter {
                    eq("receiver_id", receiverId)
                }
                select()
            }
            .decodeList<MessageResponse>()

        Log.d("SupabaseRemoteDataSource", "markAllAsReadForReceiver($receiverId) -> updated ${updated.size}")
        return updated
    }

    /**
     * Delete a message by its id.
     */
    suspend fun deleteMessageById(messageId: String) {
        postgrest.from("messages")
            .delete {
                filter {
                    eq("id", messageId)
                }
            }
        Log.d("SupabaseRemoteDataSource", "deleteMessageById($messageId) -> OK")
    }

    /**
     * Search messages by content (ilike) and optionally by sender_id.
     */
    suspend fun searchMessagesByContentOrSender(query: String, senderId: String? = null): List<MessageResponse> {
        val list = postgrest.from("messages")
            .select {
                filter {
                    // use ilike for case-insensitive partial match
                    ilike("content", "%$query%")
                    if (senderId != null) {
                        and {
                            eq("sender_id", senderId)
                        }
                    }
                }
                order("created_at", order = Order.DESCENDING)
                limit(200)
            }
            .decodeList<MessageResponse>()

        Log.d("SupabaseRemoteDataSource", "searchMessages(query=$query, sender=$senderId) -> ${list.size}")
        return list
    }

    // ------------------ Storage (kept on Retrofit for now) ------------------
    // If you want to move storage to the Jan supabase storage client, I can convert this too.

    suspend fun uploadFile(bucket: String, path: String, bytes: ByteArray, mime: String) {
        // keep using Retrofit storage endpoint for simplicity
        val resp = api.uploadFile(anonKey, authHeaderOrNull(), mime, bucket, path, bytes)
        if (!resp.isSuccessful) throw Exception("Upload failed: ${resp.code()} - ${resp.errorBody()?.string()}")
    }

    suspend fun createSignedUrl(bucket: String, path: String, expiresInSeconds: Int): String? {
        val resp = api.createSignedUrl(anonKey, authHeaderOrNull(), bucket, path, SignedUrlRequest(expiresInSeconds))
        if (!resp.isSuccessful) return null
        return resp.body()?.signedURL
    }

    // ------------------ Helpers ------------------

    private fun authHeaderOrNull(): String? {
        return userJwt.takeIf { it.isNotBlank() }?.let { "Bearer $it" }
    }

    fun updateUserJwt(newJwt: String) {
        userJwt = newJwt
    }

    // ------------------ Realtime (OkHttp WebSocket) ------------------
    // Keep existing websocket based realtime as a fallback. You may replace this with
    // the Jan supabase realtime client for cleaner integration.

    // Start websocket to listen to INSERT events on messages table
    // Auth with apikey param and join topic "realtime:public:messages"
    fun startRealtime() {
        if (webSocket != null) return

        val url = "$supabaseUrl/realtime/v1?apikey=$anonKey&vsn=1.0.0"
        val req = Request.Builder().url(url).build()

        webSocket = okClient.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                // join channel
                val join = """
                    {"topic":"realtime:public:messages","event":"phx_join","payload":{},"ref":"0"}
                """.trimIndent()
                ws.send(join)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val el = json.parseToJsonElement(text).jsonObject
                    val event = el["event"]?.jsonPrimitive?.contentOrNull
                    if (event != null && (event == "postgres_changes" || event.equals("INSERT", true))) {
                        val payload = el["payload"]?.jsonObject
                        val record = payload?.get("record")?.jsonObject
                        if (record != null) {
                            // convert JsonObject -> MessageResponse (partial mapping)
                            val id = record["id"]?.jsonPrimitive?.content ?: return
                            val roomId = record["room_id"]?.jsonPrimitive?.content ?: ""
                            val sender = record["sender_id"]?.jsonPrimitive?.content ?: ""
                            val content = record["content"]?.jsonPrimitive?.contentOrNull
                            val messageType = record["message_type"]?.jsonPrimitive?.contentOrNull ?: "text"
                            val mediaUrl = record["media_url"]?.jsonPrimitive?.contentOrNull
                            val isRead = record["is_read"]?.jsonPrimitive?.booleanOrNull ?: false
                            val createdAt = record["created_at"]?.jsonPrimitive?.content ?: ""
                            val msg = MessageResponse(
                                id = id,
                                room_id = roomId,
                                sender_id = sender,
                                content = content,
                                message_type = messageType,
                                media_url = mediaUrl,
                                metadata = null,
                                is_read = isRead,
                                created_at = createdAt
                            )
                            scope.launch { _incomingMessagesRaw.emit(msg) }
                        }
                    }
                } catch (_: Exception) { }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                // try reconnect, or let caller restart
                webSocket = null
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                webSocket = null
            }
        })
    }

    fun stopRealtime() {
        webSocket?.close(1000, "client closing")
        webSocket = null
    }
}
