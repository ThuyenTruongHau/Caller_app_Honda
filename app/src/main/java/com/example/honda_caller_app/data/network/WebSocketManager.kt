package com.example.honda_caller_app.data.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * WebSocketManager để quản lý kết nối WebSocket với backend
 */
class WebSocketManager(
    private val client: OkHttpClient,
) : WebSocketListener() {
    private var webSocket: WebSocket? = null
    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow = _messageFlow.asSharedFlow()

    fun connect(input: String) {
        val url = "ws://192.168.1.7:8001/ws/$input" // điều chỉnh path theo backend
        val requestBuilder = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(requestBuilder, this)
    }

    fun disconnect() {
        webSocket?.close(1000, "Logout")
        webSocket = null
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        _messageFlow.tryEmit(text)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        // TODO: xử lý lỗi kết nối
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(code, reason)
    }
}