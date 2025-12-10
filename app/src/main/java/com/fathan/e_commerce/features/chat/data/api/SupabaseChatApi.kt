package com.fathan.e_commerce.features.chat.data.api

import com.fathan.e_commerce.features.chat.data.model.request.InsertMessageRequest
import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
import com.fathan.e_commerce.features.chat.data.model.request.SignedUrlRequest
import com.fathan.e_commerce.features.chat.data.model.response.SignedUrlResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface SupabaseChatApi {
    // Insert a message row and return representation
    @POST("/rest/v1/messages")
    suspend fun insertMessage(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String?,
        @Header("Prefer") prefer: String = "return=representation",
        @Body body: InsertMessageRequest
    ): Response<List<MessageResponse>>

    // Fetch messages by room_id
    // To query PostgREST with room_id=eq.<roomId>, pass roomId param as "eq.<uuid>"
    @GET("/rest/v1/messages")
    suspend fun getMessagesByRoom(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String?,
        @Query("room_id") roomFilter: String, // e.g. "eq.<room_uuid>"
        @Query("order") order: String = "created_at.asc",
        @Query("limit") limit: Int = 100
    ): Response<List<MessageResponse>>

    // Fetch all latest messages grouped by room (we implement using RPC or simple messages select)
    // Here we provide a simple fetch: all messages for user (you can change to an RPC for latest-per-room)
    @GET("/rest/v1/messages")
    suspend fun getAllMessages(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String?,
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 1000
    ): Response<List<MessageResponse>>

    // Update messages: mark read by room -> PATCH /rest/v1/messages?room_id=eq.<id>
    @PATCH("/rest/v1/messages")
    suspend fun patchMessagesByRoom(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String?,
        @Query("room_id") roomFilter: String, // "eq.<room_uuid>"
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<MessageResponse>>

    // Update all messages for the current user -> you likely need an RPC; fallback: patch where receiver_id = auth.uid()
    @PATCH("/rest/v1/messages")
    suspend fun patchAllMessages(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String?,
        @Query("receiver_id") receiverFilter: String, // e.g. "eq.<myAuthId>" OR use RLS with auth.uid()
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<MessageResponse>>

    // Delete a message by id: DELETE /rest/v1/messages?id=eq.<uuid>
    @DELETE("/rest/v1/messages")
    suspend fun deleteMessageById(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String?,
        @Query("id") idFilter: String // "eq.<message_uuid>"
    ): Response<Unit>

    // Search messages: use PostgREST text search: content=ilike.%query% OR sender_id=eq.<sender>
    @GET("/rest/v1/messages")
    suspend fun searchMessages(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String?,
        @QueryMap filters: Map<String, String>,
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 200
    ): Response<List<MessageResponse>>

    // Storage upload (PUT)
    @PUT("/storage/v1/object/{bucket}/{path}")
    suspend fun uploadFile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String?,
        @Header("Content-Type") contentType: String,
        @Path("bucket") bucket: String,
        @Path(value = "path", encoded = true) path: String,
        @Body fileBytes: ByteArray
    ): Response<Unit>

    // Create signed url (optional)
    @POST("/storage/v1/object/sign/{bucket}/{path}")
    suspend fun createSignedUrl(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String?,
        @Path("bucket") bucket: String,
        @Path("path") path: String,
        @Body body: SignedUrlRequest
    ): Response<SignedUrlResponse>
}