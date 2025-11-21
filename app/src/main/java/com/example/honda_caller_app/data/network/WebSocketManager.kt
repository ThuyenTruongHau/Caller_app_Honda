package com.example.honda_caller_app.data.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketManager(
    private val client: OkHttpClient
) : WebSocketListener() {

    private var webSocket: WebSocket? = null
    private var lastUrl: String = ""

    // Auto reconnect control
    private var shouldReconnect = true
    private var retryDelay = 1000L          // start at 1s
    private val maxRetryDelay = 10000L      // max 10s
    private val handler = Handler(Looper.getMainLooper())

    private val _messageFlow = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 10
    )
    val messageFlow = _messageFlow.asSharedFlow()

    // ---------------------
    // PUBLIC API
    // ---------------------
    fun connect(input: String) {
        val url = "ws://192.168.1.7:8001/ws/$input"
        lastUrl = url
        shouldReconnect = true

        Log.d("WebSocketManager", "Connecting WebSocket: $url")

        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, this)
    }

    fun disconnect() {
        shouldReconnect = false
        retryDelay = 1000L     // Reset retry delay
        webSocket?.close(1000, "Logout")
        webSocket = null

        Log.d("WebSocketManager", "WebSocket disconnected manually")
    }

    // ---------------------
    // INTERNAL HANDLERS
    // ---------------------
    private fun scheduleReconnect() {
        if (!shouldReconnect) {
            Log.d("WebSocketManager", "Reconnect disabled. Skip.")
            return
        }

        Log.d("WebSocketManager", "Reconnecting in $retryDelay ms")

        handler.postDelayed({
            Log.d("WebSocketManager", "Attempting reconnect...")
            connect(lastUrl.substringAfterLast("/ws/"))
            retryDelay = (retryDelay * 2).coerceAtMost(maxRetryDelay)
        }, retryDelay)
    }

    // ---------------------
    // CALLBACKS
    // ---------------------
    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocketManager", "WebSocket connected!")
        retryDelay = 1000L    // Reset backoff
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocketManager", "WS Message: $text")
        _messageFlow.tryEmit(text)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocketManager", "WS Failure: ${t.message}")
        scheduleReconnect()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocketManager", "WS Closed: $reason")

        // Only reconnect if not closed manually
        if (shouldReconnect) scheduleReconnect()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocketManager", "WS Closing: $reason")
    }
}
