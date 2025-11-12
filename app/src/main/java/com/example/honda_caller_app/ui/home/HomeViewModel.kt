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
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.JsonElement
import kotlinx.coroutines.launch 

class HomeViewModel(
    private val nodeRepository: NodeRepository,
    private val webSocketRepository: SocketRepository? = null
) : ViewModel() {
    
    // State để lưu danh sách nodes theo từng loại (giữ để tương thích UI)
    var supplyNodes by mutableStateOf<List<Node>>(emptyList())
        private set
    
    var returnsNodes by mutableStateOf<List<Node>>(emptyList())
        private set
    
    var bothNodes by mutableStateOf<List<Node>>(emptyList())
        private set
    
    // State mới để lưu AdvancedNodesResponse
    var advancedNodesResponse by mutableStateOf<AdvancedNodesResponse?>(null)
        private set
    
    // State để lưu auto nodes theo từng line
    var autoNodesByLine by mutableStateOf<Map<String, List<NodeOut>>>(emptyMap())
        private set
    
    // State để lưu manual return nodes theo từng line
    var manualReturnNodeByLine by mutableStateOf<Map<String, List<NodeOut>>>(emptyMap())
        private set
    
    // Danh sách tên các line có sẵn trong nodeAuto (để biết có những line nào)
    var availableLineNames by mutableStateOf<List<String>>(emptyList())
        private set

    var availableLineForVLManual by mutableStateOf<List<String>>(emptyList())
        private set
    
    // State để track việc gửi command
    var isSendingCommand by mutableStateOf(false)
        private set
    
    var commandResult by mutableStateOf<String>("")
        private set
    
    var commandData by mutableStateOf<com.google.gson.JsonElement?>(null)
        private set

    var showResultDialog by mutableStateOf(false)
        private set
    
    // Flag để track đã fetch nodes chưa
    private var nodesFetched = false

    // State để lưu JSON message từ WebSocket (lưu JsonObject để có thể truy cập các trường động)
    var notificationJson by mutableStateOf<JsonObject?>(null)
        private set

    // State để lưu thông báo đã format để hiển thị
    var notificationTitle by mutableStateOf<String?>(null)
        private set

    var notificationText by mutableStateOf<String?>(null)
        private set

    var showNotification by mutableStateOf(false)
        private set

    init {
        // Collect messages từ WebSocket nếu có repository
        webSocketRepository?.let { repository ->
            viewModelScope.launch {
                repository.messages.collect { rawMessage ->
                    handleWebSocketMessage(rawMessage)
                }
            }
        }
    }
    
    /**
     * Gọi các API để lấy nodes theo từng loại
     */
    private suspend fun fetchAllNodes(owner: String) {
        // Gọi advanced API mới
        when (val result = nodeRepository.getAdvancedNodes(owner)) {
            is NetworkResult.Success -> {
                advancedNodesResponse = result.data
                
                // Gán biến nodeAuto từ vl_nodes["auto"] với xử lý exception
                val node = result.data
                val nodeAuto = node.vl_nodes["auto"] // nodeAuto có kiểu Map<String, List<NodeOut>>?
                val nodeManualReturn = node.vl_nodes["returns"] // nodeManualReturn có kiểu Map<String, List<NodeOut>>?
                val supplyNodes = node.pt_nodes["supply"]
                val returnsNodes = node.pt_nodes["returns"]
                val bothNodes = node.pt_nodes["both"]
                
                // Xử lý trường hợp không có node "auto" và lấy nodes theo từng line
                // nodeAuto là Map<String, List<NodeOut>> với key là tên line (ví dụ: "Line 1", "Line 2", "Line 3")
                autoNodesByLine = nodeAuto ?: emptyMap()
                manualReturnNodeByLine = nodeManualReturn ?: emptyMap()
                
                // Lấy danh sách tên các line có sẵn (để biết có những line nào)
                availableLineNames = autoNodesByLine.keys.sorted() // Sắp xếp để dễ đọc
                availableLineForVLManual = manualReturnNodeByLine.keys.sorted()


            }
            is NetworkResult.Error -> {
                // Xử lý lỗi nếu cần
                advancedNodesResponse = null
            }
            is NetworkResult.Loading -> {
                // Loading state
            }
        }
    }
    
    /**
     * Lấy nodes của một line cụ thể từ nodeAuto
     * @param lineName Tên line (ví dụ: "Line 1", "Line 2", "Line 3")
     * @return Danh sách NodeOut của line đó, hoặc emptyList() nếu không tìm thấy
     */
    fun getNodesByLine(lineName: String): List<NodeOut> {
        return autoNodesByLine[lineName] ?: emptyList()
    }

    fun getManualNodesReturn(lineName: String): List<NodeOut> {
        return manualReturnNodeByLine[lineName] ?: emptyList()
    }
    
    /**
     * Lấy manual return nodes của một line cụ thể từ vl_nodes["returns"]
     * @param lineName Tên line (ví dụ: "Line 1", "Line 2", "Line 3")
     * @return Danh sách NodeOut của line đó, hoặc emptyList() nếu không tìm thấy
     */
    fun getManualReturnNodesByLine(lineName: String): List<NodeOut> {
        return manualReturnNodeByLine[lineName] ?: emptyList()
    }
    
    /**
     * Kiểm tra xem một line có tồn tại trong nodeAuto không
     * @param lineName Tên line cần kiểm tra
     * @return true nếu line tồn tại, false nếu không
     */
    fun hasLine(lineName: String): Boolean {
        return autoNodesByLine.containsKey(lineName)
    }
    
    /**
     * Lấy tất cả nodes của tất cả các line (flatten thành một list)
     * @return Danh sách tất cả NodeOut từ tất cả các line
     */
    fun getAllAutoNodes(): List<NodeOut> {
        return autoNodesByLine.values.flatten()
    }
    
    /**
     * Gọi API lấy nodes nếu chưa có (cho trường hợp đã đăng nhập từ trước)
     */
    suspend fun fetchNodesIfNeeded(owner: String) {
        // Chỉ gọi API nếu chưa fetch
        if (!nodesFetched) {
            fetchAllNodes(owner)
            nodesFetched = true
        }
    }
    
    /**
     * Gửi lệnh RCS khi click vào node
     */
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
                    commandData = result.data  // Lưu data trả về
                    showResultDialog = true  // Hiển thị dialog
                }
                is NetworkResult.Error -> {
                    isSendingCommand = false
                    commandResult = result.message
                    showResultDialog = true  // Hiển thị dialog lỗi
                }
                is NetworkResult.Loading -> {
                    // Loading state
                }
            }
        }
    }

    fun dismissResultDialog() {
        showResultDialog = false
    }

    /**
     * Extension function để lấy String từ JsonObject một cách an toàn
     * Tránh conflict với Kotlin extension function yêu cầu API 26
     * ĐẶT TRƯỚC CLASS HomeViewModel
     */
    private fun JsonObject.optString(key: String): String? {
        return if (this.has(key) && !this.get(key).isJsonNull) {
            val element: JsonElement = this.get(key)
            if (element.isJsonPrimitive) {
                element.asJsonPrimitive.asString
            } else {
                null
            }
        } else {
            null
        }
    }

    /**
     * Xử lý message từ WebSocket - Parse JSON động
     * @param rawMessage JSON string từ WebSocket
     */
    private fun handleWebSocketMessage(rawMessage: String) {
        try {
            // Parse JSON thành JsonObject (không cần data model)
            val jsonObject: JsonObject = JsonParser().parse(rawMessage).asJsonObject

            // Lưu JsonObject để có thể truy cập các trường sau này
            notificationJson = jsonObject

            // Extract các trường một cách động - Dùng extension function optString
            val message = jsonObject.optString("message")
                ?: jsonObject.optString("content")
                ?: jsonObject.optString("text")
                ?: jsonObject.optString("msg")
                ?: rawMessage

            val title = jsonObject.optString("title")
                ?: jsonObject.optString("subject")
                ?: jsonObject.optString("type")
                ?: "Thông báo"

            // Lấy type nếu có
            val type = jsonObject.optString("type")

            // Log tất cả các trường có trong JSON để debug
            jsonObject.keySet().forEach { key ->
                val element: JsonElement = jsonObject.get(key)
            }


            // Cập nhật state để hiển thị
            notificationTitle = title
            notificationText = message
            showNotification = true

        } catch (e: JsonSyntaxException) {
            // Nếu không parse được JSON, hiển thị raw message
            // Set notificationJson = null vì không parse được
            notificationJson = null

            // Hiển thị raw message như một thông báo đơn giản
            notificationTitle = "Thông báo"
            notificationText = rawMessage
            showNotification = true
        } catch (e: Exception) {
            // Set notificationJson = null vì không parse được
            notificationJson = null

            // Hiển thị raw message như một thông báo đơn giản
            notificationTitle = "Thông báo"
            notificationText = rawMessage
            showNotification = true
        }
    }

    /**
     * Lấy giá trị của một trường bất kỳ từ JSON message
     * @param key Tên trường cần lấy
     * @return Giá trị của trường dưới dạng String, hoặc null nếu không tồn tại
     */
    fun getNotificationField(key: String): String? {
        return notificationJson?.get(key)?.asString
    }

    /**
     * Lấy giá trị của một trường bất kỳ từ JSON message (dạng số)
     * @param key Tên trường cần lấy
     * @return Giá trị của trường dưới dạng Int, hoặc null nếu không tồn tại
     */
    fun getNotificationFieldInt(key: String): Int? {
        return notificationJson?.get(key)?.asInt
    }

    /**
     * Kiểm tra xem JSON message có chứa trường nào không
     * @param key Tên trường cần kiểm tra
     * @return true nếu có, false nếu không
     */
    fun hasNotificationField(key: String): Boolean {
        return notificationJson?.has(key) == true
    }

    /**
     * Lấy toàn bộ JSON message dưới dạng string (để hiển thị chi tiết)
     * @return JSON string, hoặc null nếu không có
     */
    fun getNotificationJsonString(): String? {
        return notificationJson?.toString()
    }

    /**
     * Ẩn thông báo
     */
    fun dismissNotification() {
        showNotification = false
        notificationJson = null
        notificationTitle = null
        notificationText = null
    }

    /**
     * Reset state khi logout
     */
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
        isSendingCommand = false
        commandResult = ""
        commandData = null
        showResultDialog = false
        showNotification = false
        notificationJson = null
        notificationTitle = null
        notificationText = null
    }

}
