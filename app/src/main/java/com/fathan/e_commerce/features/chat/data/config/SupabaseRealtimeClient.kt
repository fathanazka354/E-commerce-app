package com.fathan.e_commerce.features.chat.data.config

import com.fathan.e_commerce.features.chat.data.model.response.MessageResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class SupabaseRealtimeClient(
    private val supabaseUrl: String,
    private val accessToken: String
) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect(roomId: String, onMessage: (MessageResponse) -> Unit) {
        val req = Request.Builder()
            .url("$supabaseUrl/realtime/v1/websocket?apikey=$accessToken&vsn=1.0.0")
            .build()

        webSocket = client.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                val joinPayload = """
                    {
                      "topic": "realtime:public:messages",
                      "event": "phx_join",
                      "payload": {},
                      "ref": "1"
                    }
                """.trimIndent()

                ws.send(joinPayload)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                if ("INSERT" in text) {
                    try {
                        val json = Json.Default.parseToJsonElement(text)

                        val record = json.jsonObject["payload"]!!
                            .jsonObject["record"]!!
                            .jsonObject

                        val msg = MessageResponse(
                            id = record["id"]!!.jsonPrimitive.content,
                            room_id = record["room_id"]!!.jsonPrimitive.content,
                            sender_id = record["sender_id"]!!.jsonPrimitive.content,
                            content = record["content"]!!.jsonPrimitive.content,
                            message_type = record["message_type"]!!.jsonPrimitive.content,
                            media_url = record["media_url"]?.jsonPrimitive?.content,
                            created_at = record["created_at"]!!.jsonPrimitive.content
                        )

                        onMessage(msg)
                    } catch (_: Exception) {}
                }
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "bye")
    }
}