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
import kotlinx.coroutines.launch 

class HomeViewModel(
    private val nodeRepository: NodeRepository
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
        // Gọi advanced API mới
        when (val result = nodeRepository.getAdvancedNodes(owner)) {
            is NetworkResult.Success -> {
                advancedNodesResponse = result.data
                
                // Flatten data để tương thích với UI hiện tại
                // Tạm thời giữ nguyên logic cũ, sẽ sửa UI sau
                flattenAdvancedNodes(result.data)
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
     * Flatten AdvancedNodesResponse thành các list đơn giản để tương thích UI hiện tại
     * Tạm thời để merge tất cả nodes, sau này sẽ chỉnh theo UI mới
     */
    private fun flattenAdvancedNodes(response: AdvancedNodesResponse) {
        val allSupplyNodes = mutableListOf<Node>()
        val allReturnsNodes = mutableListOf<Node>()
        val allBothNodes = mutableListOf<Node>()
        val allAutoNodes = mutableListOf<Node>()
        
        // Helper function để flatten nodes từ một NodesByType
        fun flattenNodesByType(nodesByType: Map<String, Map<String, List<NodeOut>>>) {
            nodesByType.forEach { (nodeType, linesMap) ->
                linesMap.values.forEach { nodes ->
                    when (nodeType) {
                        "supply" -> allSupplyNodes.addAll(nodes)
                        "returns" -> allReturnsNodes.addAll(nodes)
                        "both" -> allBothNodes.addAll(nodes)
                        "auto" -> allAutoNodes.addAll(nodes)
                    }
                }
            }
        }
        
        // Lấy từ pt_nodes
        flattenNodesByType(response.pt_nodes)
        
        // Lấy từ vl_nodes
        flattenNodesByType(response.vl_nodes)
        
        // Gán vào state (tạm thời giữ tương thích với UI cũ)
        supplyNodes = allSupplyNodes
        returnsNodes = allReturnsNodes
        bothNodes = allBothNodes 
        // TODO: Thêm state cho allAutoNodes nếu cần
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
    
    /**
     * Reset state khi logout
     */
    fun resetState() {
        supplyNodes = emptyList()
        returnsNodes = emptyList()
        bothNodes = emptyList()
        advancedNodesResponse = null
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
