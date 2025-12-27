package com.fathan.e_commerce.features.chat.data.source

import android.util.Log
import com.fathan.e_commerce.features.chat.data.api.SupabaseChatApi
import com.fathan.e_commerce.features.chat.data.model.request.InsertMessageRequest
import com.fathan.e_commerce.features.chat.data.model.request.SignedUrlRequest
import com.fathan.e_commerce.features.chat.data.model.response.ConversationResponse
import com.fathan.e_commerce.features.chat.data.model.response.MessageDetailResponse
import com.fathan.e_commerce.features.chat.data.model.response.MessageReadRecord
import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
import com.fathan.e_commerce.features.chat.data.model.response.MessageWithReadStatus
import com.fathan.e_commerce.features.chat.data.model.response.RoomMemberRow
import com.fathan.e_commerce.features.chat.data.model.response.RoomResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.MDC.put
import java.util.concurrent.TimeUnit

class SupabaseRemoteDataSource(
    private val api: SupabaseChatApi,
    private val postgrest: Postgrest,
    private val supabaseClient: SupabaseClient,
    private val supabaseUrl: String,
    private val anonKey: String,
    private var userJwt: String
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _incomingMessagesRaw = MutableSharedFlow<MessageResponse>(replay = 50)
    val incomingMessagesRaw = _incomingMessagesRaw.asSharedFlow()

    private val okClient: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private var webSocket: WebSocket? = null

    suspend fun insertMessage(payload: InsertMessageRequest): MessageResponse {
        try {
            Log.d("CHAT", "=== INSERT MESSAGE DIRECT ===")
            Log.d("CHAT", "Room: ${payload.roomId}")
            Log.d("CHAT", "Type: ${payload.messageType}")

            val result = postgrest
                .from("messages")
                .insert(payload) {
                    select()  // Atau bisa spesifik: select("*")
                }
                .decodeSingle<MessageResponse>()

            Log.d("CHAT", "✅ INSERT OK: ${result.messageId}")
            return result

        } catch (e: Exception) {
            Log.e("CHAT", "❌ INSERT FAILED", e)
            throw e
        }
    }

    suspend fun fetchMessagesByRoom(roomId: String): List<MessageResponse> {
        return postgrest
            .from("message_detail_with_status_read")
            .select {
                filter {
                    eq("room_id", roomId)
                }
                order("created_at", Order.ASCENDING)
                limit(1000)
            }
            .decodeList()
    }

    suspend fun fetchMessagesWithReadStatus(
        roomId: String
    ): List<MessageWithReadStatus> {

        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        return postgrest
            .rpc(
                "message_detail_with_status_read",
                mapOf(
                    "p_room_id" to roomId,
                    "p_user_id" to currentUserId
                )
            )
            .decodeList<MessageWithReadStatus>()
            .map { msg ->
                MessageWithReadStatus(
                    messageId = msg.messageId,
                    roomId = msg.roomId,
                    senderId = msg.senderId,
                    content = msg.content,
                    messageType = msg.messageType,
                    mediaUrl = msg.mediaUrl,
                    metadata = msg.metadata, // optional
                    createdAt = msg.createdAt,

                    productId = msg.productId,
                    productTitle = msg.productTitle,
                    productPrice = msg.productPrice,
                    productImage = msg.productImage,

                    senderName = msg.senderName,

                    buyerName =
                        if (msg.roomBuyerId == currentUserId) msg.sellerName else msg.buyerName,

                    roomSellerId =
                        if (msg.roomBuyerId == currentUserId) msg.roomSellerId else msg.roomBuyerId,

                    isRead = msg.isRead,
                    readAt = msg.readAt,
//                    isMe = msg.senderId == currentUserId,
                    sellerName = msg.sellerName,
                    roomBuyerId = msg.roomBuyerId
                )
            }
    }

    suspend fun fetchConversations(): List<ConversationResponse> {
        return try {
            postgrest
                .from("conversation_list")
                .select()
                .decodeList<ConversationResponse>()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching conversations: ${e.message}")
            emptyList()
        }
    }


    suspend fun findPrivateRoomBetween(userA: String, userB: String): RoomResponse? {
        val roomsOfA = postgrest.from("room_members")
            .select {
                filter { eq("user_id", userA) }
            }
            .decodeList<RoomMemberRow>()

        val roomIds = roomsOfA.map { it.room_id }.distinct()
        if (roomIds.isEmpty()) return null

        val candidateRooms = postgrest.from("rooms")
            .select {
                filter {
                    isIn("id", roomIds)
                    eq("is_group", false)
                }
            }
            .decodeList<RoomResponse>()

        for (room in candidateRooms) {
            val members = postgrest.from("room_members")
                .select {
                    filter { eq("room_id", room.id) }
                }
                .decodeList<RoomMemberRow>()

            if (members.any { it.user_id == userB }) {
                return room
            }
        }

        return null
    }

    suspend fun createRoomIfNotExists(userA: String, userB: String): RoomResponse {
        val existing = findPrivateRoomBetween(userA, userB)
        if (existing != null) return existing

        val created = postgrest.from("rooms")
            .insert(
                mapOf(
                    "title" to null,
                    "is_group" to false,
                    "created_by" to userA
                )
            ) {
                select()
            }
            .decodeSingle<RoomResponse>()

        postgrest.from("room_members")
            .insert(
                listOf(
                    mapOf("room_id" to created.id, "user_id" to userA),
                    mapOf("room_id" to created.id, "user_id" to userB)
                )
            )

        return created
    }

    suspend fun markReadByRoom(roomId: String) {
        postgrest.rpc(
            "mark_room_as_read",
            mapOf("p_room_id" to roomId)
        )
    }

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

    suspend fun deleteMessageById(messageId: String) {
        postgrest.from("messages")
            .delete {
                filter {
                    eq("id", messageId)
                }
            }
        Log.d("SupabaseRemoteDataSource", "deleteMessageById($messageId) -> OK")
    }

    suspend fun searchMessagesByContentOrSender(query: String, senderId: String? = null): List<MessageResponse> {
        val list = postgrest.from("messages")
            .select {
                filter {
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

    // ------------------ Storage ------------------

    suspend fun uploadFile(
        bucket: String,
        path: String,
        bytes: ByteArray,
        mime: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validasi input
            require(bucket.isNotBlank()) { "Bucket name cannot be empty" }
            require(path.isNotBlank()) { "Path cannot be empty" }
            require(bytes.isNotEmpty()) { "File bytes cannot be empty" }
            require(mime.isNotBlank()) { "MIME type cannot be empty" }

            Log.d(TAG, "=== UPLOAD FILE START ===")
            Log.d(TAG, "Bucket: $bucket")
            Log.d(TAG, "Path: $path")
            Log.d(TAG, "Size: ${formatFileSize(bytes.size)}")
            Log.d(TAG, "MIME: $mime")

            // Create request body
            val body = bytes.toRequestBody(mime.toMediaType())

            // Upload file
            val response = api.uploadFile(
                apiKey = anonKey,
                auth = authHeaderOrNull() ?: run {
                    Log.e(TAG, "No auth header available")
                    throw AuthenticationException("User not authenticated")
                },
                contentType = mime,
                bucket = bucket,
                path = path,
                body = body
            )

            Log.d(TAG, "Response code: ${response.code()}")

            // Handle response
            when {
                response.isSuccessful -> {
                    val fileUrl = buildFileUrl(bucket, path)
                    Log.d(TAG, "✅ Upload SUCCESS")
                    Log.d(TAG, "File URL: $fileUrl")
                    Result.success(fileUrl)
                }

                response.code() == 401 -> {
                    Log.e(TAG, "❌ Unauthorized - Invalid or expired token")
                    Result.failure(AuthenticationException("Unauthorized: Please login again"))
                }

                response.code() == 403 -> {
                    Log.e(TAG, "❌ Forbidden - No permission to upload")
                    Result.failure(PermissionException("No permission to upload to this bucket"))
                }

                response.code() == 413 -> {
                    Log.e(TAG, "❌ File too large")
                    Result.failure(FileSizeException("File size exceeds limit"))
                }

                else -> {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Upload failed: ${response.code()}")
                    Log.e(TAG, "Error body: $errorBody")
                    Result.failure(
                        UploadException(
                            "Upload failed with code ${response.code()}: ${errorBody ?: "Unknown error"}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Upload exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun buildFileUrl(bucket: String, path: String): String {
        val baseUrl = "https://${supabaseUrl.removePrefix("https://")}"
        return "$baseUrl/storage/v1/object/public/$bucket/$path"
    }

    /**
     * Format file size untuk logging
     */
    private fun formatFileSize(bytes: Int): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }


    suspend fun createSignedUrl(bucket: String, path: String, expiresInSeconds: Int): String? {
        Log.d("Storage", "=== CREATE SIGNED URL ===")
        Log.d("Storage", "Bucket: $bucket")
        Log.d("Storage", "Path: $path")
        Log.d("Storage", "Expires: $expiresInSeconds seconds")

        val resp = api.createSignedUrl(
            anonKey,
            authHeaderOrNull(),
            bucket,
            path,
            SignedUrlRequest(expiresInSeconds)
        )

        Log.d("Storage", "Response code: ${resp.code()}")

        if (!resp.isSuccessful) {
            val errorBody = resp.errorBody()?.string()
            Log.e("Storage", "Failed to create signed URL: ${resp.code()} - $errorBody")
            return null
        }

        val responseBody = resp.body()
        Log.d("Storage", "Response body: $responseBody")

        val apiUrl = responseBody?.signedURL
        Log.d("Storage", "API returned signedURL: $apiUrl")

        if (apiUrl == null) {
            Log.e("Storage", "signedURL is null in response")
            return null
        }

        // ✅ Pastikan URL lengkap dengan /storage/v1
        val finalUrl = when {
            // Sudah full URL (starts with http/https)
            apiUrl.startsWith("http") -> {
                Log.d("Storage", "URL is already full")
                apiUrl
            }
            // Starts with /storage/v1/ - tambah base URL
            apiUrl.startsWith("/storage/v1/") -> {
                Log.d("Storage", "URL starts with /storage/v1/, prepending base")
                "$supabaseUrl$apiUrl"
            }
            // Starts with storage/v1/ - tambah base URL + /
            apiUrl.startsWith("storage/v1/") -> {
                Log.d("Storage", "URL starts with storage/v1/, prepending base")
                "$supabaseUrl/$apiUrl"
            }
            // Starts with /object/ - tambah /storage/v1
            apiUrl.startsWith("/object/") -> {
                Log.d("Storage", "URL starts with /object/, adding /storage/v1")
                "$supabaseUrl/storage/v1$apiUrl"
            }
            // Starts with object/ - tambah /storage/v1/
            apiUrl.startsWith("object/") -> {
                Log.d("Storage", "URL starts with object/, adding /storage/v1/")
                "$supabaseUrl/storage/v1/$apiUrl"
            }
            // Unknown format
            else -> {
                Log.w("Storage", "Unknown URL format: $apiUrl")
                "$supabaseUrl/storage/v1/object/$apiUrl"
            }
        }

        Log.d("Storage", "Final signed URL: $finalUrl")
        return finalUrl
    }

    // ------------------ Helpers ------------------

    private fun authHeaderOrNull(): String? {
        val token = supabaseClient.auth.currentSessionOrNull()?.accessToken
        return token?.let { "Bearer $it" }
    }

    // ------------------ Realtime (OkHttp WebSocket) ------------------

    fun startRealtime() {
        if (webSocket != null) return

        val url = "$supabaseUrl/realtime/v1?apikey=$anonKey&vsn=1.0.0"
        val req = Request.Builder().url(url).build()

        Log.d("Realtime", "Connecting...")

        webSocket = okClient.newWebSocket(req, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("Realtime", "Connected")
                joinChannel(ws)
                startHeartbeat(ws)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val root = json.parseToJsonElement(text).jsonObject
                    val event = root["event"]?.jsonPrimitive?.content ?: return

                    if (event != "postgres_changes") return

                    val payload = root["payload"]?.jsonObject ?: return
                    if (payload["type"]?.jsonPrimitive?.content != "INSERT") return

                    val record = payload["record"] ?: return

                    val message = json.decodeFromJsonElement(
                        MessageResponse.serializer(),
                        record
                    )

                    scope.launch {
                        _incomingMessagesRaw.emit(message)
                    }

                } catch (e: Exception) {
                    Log.e("Realtime", "Parse error", e)
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("Realtime", "Socket failure", t)
                cleanupRealtime()
                scheduleReconnect()
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d("Realtime", "Socket closed: $reason")
                cleanupRealtime()
                scheduleReconnect()
            }
        })
    }

    private fun cleanupRealtime() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        reconnectJob?.cancel()
        reconnectJob = null
        webSocket = null
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(3_000)
            Log.d("Realtime", "Reconnecting...")
            startRealtime()
        }
    }

    private fun startHeartbeat(ws: WebSocket) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (true) {
                delay(25_000)
                val heartbeat = """
            {
              "topic": "phoenix",
              "event": "heartbeat",
              "payload": {},
              "ref": "hb"
            }
            """.trimIndent()
                ws.send(heartbeat)
                Log.d("Realtime", "Heartbeat sent")
            }
        }
    }

    private fun joinChannel(ws: WebSocket) {
        val join = """
    {
      "topic": "realtime:public:messages",
      "event": "phx_join",
      "payload": {
        "config": {
          "postgres_changes": [
            {
              "event": "INSERT",
              "schema": "public",
              "table": "messages"
            }
          ]
        }
      },
      "ref": "1"
    }
    """.trimIndent()

        ws.send(join)
    }

    fun stopRealtime() {
        heartbeatJob?.cancel()
        reconnectJob?.cancel()
        webSocket?.close(1000, "client closed")
        webSocket = null
    }

    companion object {
        private const val TAG = "SupabaseStorage"
        private const val MAX_FILE_SIZE = 50 * 1024 * 1024 // 50MB
    }
}


// Custom Exceptions
class AuthenticationException(message: String) : Exception(message)
class PermissionException(message: String) : Exception(message)
class FileSizeException(message: String) : Exception(message)
class UploadException(message: String) : Exception(message)