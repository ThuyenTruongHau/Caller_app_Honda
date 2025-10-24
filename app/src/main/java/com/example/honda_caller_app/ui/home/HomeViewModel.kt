package com.example.honda_caller_app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.honda_caller_app.data.api.Node
import com.example.honda_caller_app.data.api.Payload
import com.example.honda_caller_app.data.network.NetworkResult
import com.example.honda_caller_app.data.repository.NodeRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val nodeRepository: NodeRepository
) : ViewModel() {
    
    // State để lưu danh sách nodes theo từng loại
    var supplyNodes by mutableStateOf<List<Node>>(emptyList())
        private set
    
    var returnsNodes by mutableStateOf<List<Node>>(emptyList())
        private set
    
    var bothNodes by mutableStateOf<List<Node>>(emptyList())
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
    
    /**
     * Gọi các API để lấy nodes theo từng loại
     */
    private suspend fun fetchAllNodes(owner: String) {
        // Gọi cả 3 API đồng thời
        val supplyResult = nodeRepository.getNodesByOwnerAndType(owner, "supply")
        if (supplyResult is NetworkResult.Success) {
            supplyNodes = supplyResult.data
        }
        
        val returnsResult = nodeRepository.getNodesByOwnerAndType(owner, "returns")
        if (returnsResult is NetworkResult.Success) {
            returnsNodes = returnsResult.data
        }
        
        val bothResult = nodeRepository.getNodesByOwnerAndType(owner, "both")
        if (bothResult is NetworkResult.Success) {
            bothNodes = bothResult.data
        }
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
                start = node.start,
                end = node.end,
                next_start = node.next_start,
                next_end = node.next_end
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
    
    /**
     * Reset state khi logout
     */
    fun resetState() {
        supplyNodes = emptyList()
        returnsNodes = emptyList()
        bothNodes = emptyList()
        nodesFetched = false
        isSendingCommand = false
        commandResult = ""
        commandData = null
        showResultDialog = false
    }

    fun dismissResultDialog() {
        showResultDialog = false
    }
}
