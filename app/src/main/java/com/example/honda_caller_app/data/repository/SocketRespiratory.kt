package com.example.honda_caller_app.data.repository


import com.example.honda_caller_app.data.network.WebSocketManager
import kotlinx.coroutines.flow.SharedFlow

class SocketRepository(private val webSocketManager: WebSocketManager) {

    val messages: SharedFlow<String> = webSocketManager.messageFlow

    fun connect(url: String) {
        webSocketManager.connect(url)
    }
    fun disconnect() = webSocketManager.disconnect()
}
