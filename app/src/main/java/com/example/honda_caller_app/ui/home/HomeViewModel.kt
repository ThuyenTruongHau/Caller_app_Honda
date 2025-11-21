package com.example.honda_caller_app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.honda_caller_app.data.api.Node
import com.example.honda_caller_app.data.api.NodeOut
import com.example.honda_caller_app.data.api.Payload
import com.example.honda_caller_app.data.api.AdvancedNodesResponse
import com.example.honda_caller_app.data.network.NetworkResult
import com.example.honda_caller_app.data.repository.NodeRepository
import com.example.honda_caller_app.data.repository.SocketRepository
import com.google.gson.*
import kotlinx.coroutines.launch
import android.util.Log

// ==============================
// DATA CLASS
// ==============================
data class NotificationItem(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val message: String,
    val timestamp: String?,
    val type: String?,
    val status: String?,
    val priority: String?,
    val jsonData: JsonObject?
)

// ==============================
// VIEWMODEL
// ==============================
class HomeViewModel(
    private val nodeRepository: NodeRepository,
    private val webSocketRepository: SocketRepository? = null
) : ViewModel() {

    // ---- Nodes ----
    var supplyNodes by mutableStateOf<List<Node>>(emptyList())
        private set
    var returnsNodes by mutableStateOf<List<Node>>(emptyList())
        private set
    var bothNodes by mutableStateOf<List<Node>>(emptyList())
        private set

    var advancedNodesResponse by mutableStateOf<AdvancedNodesResponse?>(null)
        private set

    var autoNodesByLine by mutableStateOf<Map<String, List<NodeOut>>>(emptyMap())
        private set
    var manualReturnNodeByLine by mutableStateOf<Map<String, List<NodeOut>>>(emptyMap())
        private set

    var availableLineNames by mutableStateOf<List<String>>(emptyList())
        private set
    var availableLineForVLManual by mutableStateOf<List<String>>(emptyList())
        private set

    var isSendingCommand by mutableStateOf(false)
        private set
    var commandResult by mutableStateOf("")
        private set
    var commandData by mutableStateOf<JsonElement?>(null)
        private set
    var showResultDialog by mutableStateOf(false)
        private set

    private var nodesFetched = false

    // =====================================================
    //               NOTIFICATION STORAGE
    // =====================================================
    var notifications by mutableStateOf<List<NotificationItem>>(emptyList())   // lịch sử
        private set

    private val notificationQueue = mutableListOf<NotificationItem>()           // HÀNG ĐỢI
    private val MAX_QUEUE = 50

    var currentNotification by mutableStateOf<NotificationItem?>(null)
        private set

    var showNotification by mutableStateOf(false)
        private set

    // Các trường cũ giữ để HomeScreen không lỗi
    var notificationJson by mutableStateOf<JsonObject?>(null)
        private set
    var notificationTitle by mutableStateOf<String?>(null)
        private set
    var notificationText by mutableStateOf<String?>(null)
        private set
    var notificationType by mutableStateOf<String?>(null)
        private set
    var notificationTimestamp by mutableStateOf<String?>(null)
        private set
    var notificationStatus by mutableStateOf<String?>(null)
        private set
    var notificationPriority by mutableStateOf<String?>(null)
        private set

    // =====================================================
    //                      INIT
    // =====================================================
    init {
        webSocketRepository?.let { repo ->
            viewModelScope.launch {
                repo.messages.collect { raw ->
                    handleWebSocketMessage(raw)
                }
            }
        }
    }

    // =====================================================
    //                    FUNCTIONS
    // =====================================================

    suspend fun fetchNodesIfNeeded(owner: String) {
        if (!nodesFetched) {
            fetchAllNodes(owner)
            nodesFetched = true
        }
    }

    private suspend fun fetchAllNodes(owner: String) {
        when (val result = nodeRepository.getAdvancedNodes(owner)) {
            is NetworkResult.Success -> {
                advancedNodesResponse = result.data
                val node = result.data

                autoNodesByLine = node.vl_nodes["auto"] ?: emptyMap()
                manualReturnNodeByLine = node.vl_nodes["returns"] ?: emptyMap()

                availableLineNames = autoNodesByLine.keys.sorted()
                availableLineForVLManual = manualReturnNodeByLine.keys.sorted()
            }
            is NetworkResult.Error -> advancedNodesResponse = null
            else -> {}
        }
    }

    fun getNodesByLine(lineName: String): List<NodeOut> =
        autoNodesByLine[lineName] ?: emptyList()

    fun getManualNodesReturn(lineName: String): List<NodeOut> =
        manualReturnNodeByLine[lineName] ?: emptyList()

    fun sendNodeCommand(node: Node) {
        isSendingCommand = true
        commandResult = ""
        commandData = null

        viewModelScope.launch {
            val payload = Payload(
                node_name = node.node_name,
                node_type = node.node_type,
                owner = node.owner,
                process_code = node.process_code,
                start = node.start,
                end = node.end,
                next_start = node.next_start,
                next_end = node.next_end,
                line = node.line
            )

            when (val result = nodeRepository.sendRcsCommand(payload)) {
                is NetworkResult.Success -> {
                    isSendingCommand = false
                    commandResult = "Thành công"
                    commandData = result.data
                    showResultDialog = true
                }
                is NetworkResult.Error -> {
                    isSendingCommand = false
                    commandResult = result.message
                    showResultDialog = true
                }
                else -> {}
            }
        }
    }

    fun dismissResultDialog() {
        showResultDialog = false
    }


    // =====================================================
    //              NOTIFICATION QUEUE HANDLING
    // =====================================================

    private fun enqueueNotification(item: NotificationItem) {
        // Chỉ hiển thị notification có type = "notification"
        if (item.type != "notification") {
            return  // Không thêm vào queue, không hiển thị
        } else {
            // Lưu lịch sử (luôn lưu tất cả)
            notifications = listOf(item) + notifications

            if (notificationQueue.size >= MAX_QUEUE) notificationQueue.removeAt(0)
            notificationQueue.add(item)

            // Nếu chưa hiển thị gì → bật luôn cái đầu
            if (currentNotification == null && !showNotification) {
                showNextFromQueue()
            }
        }
    }

    private fun showNextFromQueue() {
        if (notificationQueue.isEmpty()) {
            dismissNotification()
            return
        }

        val next = notificationQueue.removeAt(0)
        currentNotification = next
        notificationJson = next.jsonData
        notificationTitle = next.title
        notificationText = next.message
        notificationTimestamp = next.timestamp
        notificationType = next.type
        notificationStatus = next.status
        notificationPriority = next.priority

        showNotification = true
    }

    fun onNotificationDismissed() {
        showNotification = false
        currentNotification = null

        // Thêm delay 1 giây trước khi show thông báo tiếp theo
        viewModelScope.launch {
            kotlinx.coroutines.delay(20L)
            showNextFromQueue()
        }
    }

    fun resumeNotificationQueue() {
        if (currentNotification == null && notificationQueue.isNotEmpty()) {
            showNextFromQueue()
        }
    }

    // =====================================================
    //                  WEBSOCKET PROCESSING
    // =====================================================

    private fun JsonObject.optString(key: String): String? {
        return if (this.has(key) && !this.get(key).isJsonNull)
            this.get(key).asString
        else null
    }

    private fun handleWebSocketMessage(raw: String) {
        try {
            val parsed = JsonParser().parse(raw)

            if (parsed.isJsonObject) {
                val obj = parsed.asJsonObject

                val message = obj.optString("alarm_code")
                    ?: obj.optString("message")
                    ?: raw

                val title = obj.optString("device_name")
                    ?: obj.optString("title")
                    ?: "Thông báo"

                val type = obj.optString("type")
                val timestamp = obj.optString("alarm_date")
                val status = obj.optString("alarm_status")
                val priority = obj.optString("alarm_grade")

                val item = NotificationItem(
                    title = title,
                    message = message,
                    timestamp = timestamp,
                    type = type,
                    status = status,
                    priority = priority,
                    jsonData = obj
                )

                enqueueNotification(item)
                return
            }

            // Không phải JSON object
            enqueueNotification(
                NotificationItem(
                    title = "Thông báo",
                    message = raw,
                    timestamp = null,
                    type = null,
                    status = null,
                    priority = null,
                    jsonData = null
                )
            )

        } catch (e: Exception) {
            enqueueNotification(
                NotificationItem(
                    title = "Thông báo",
                    message = raw,
                    timestamp = null,
                    type = null,
                    status = null,
                    priority = null,
                    jsonData = null
                )
            )
        }
    }


    // =====================================================
    //              API DÙNG BỞI UI CŨ
    // =====================================================
    fun dismissNotification() {
        showNotification = false
        currentNotification = null
        notificationJson = null
        notificationTitle = null
        notificationText = null
        notificationType = null
        notificationTimestamp = null
        notificationStatus = null
        notificationPriority = null
    }

    fun clearAllNotifications() {
        notifications = emptyList()
    }

    fun removeNotification(notificationId: String) {
        notifications = notifications.filter { it.id != notificationId }
    }

    fun resetState() {
        supplyNodes = emptyList()
        returnsNodes = emptyList()
        bothNodes = emptyList()
        advancedNodesResponse = null
        autoNodesByLine = emptyMap()
        manualReturnNodeByLine = emptyMap()
        availableLineNames = emptyList()
        availableLineForVLManual = emptyList()
        nodesFetched = false

        // Reset notification
        notifications = emptyList()
        notificationQueue.clear()
        currentNotification = null
        showNotification = false

        notificationJson = null
        notificationTitle = null
        notificationText = null
        notificationType = null
        notificationTimestamp = null
        notificationStatus = null
        notificationPriority = null
    }
}
