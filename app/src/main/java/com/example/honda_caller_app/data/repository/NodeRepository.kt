package com.example.honda_caller_app.data.repository

import com.example.honda_caller_app.data.api.ApiService
import com.example.honda_caller_app.data.api.Node
import com.example.honda_caller_app.data.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import com.example.honda_caller_app.data.api.Payload
import com.google.gson.JsonElement

/**
 * NodeRepository để quản lý các API calls liên quan đến nodes
 */
class NodeRepository(
    private val apiService: ApiService
) {
    
    /**
     * Lấy danh sách nodes theo owner và node type
     * @param owner Username của người dùng
     * @param nodeType Loại node: supply, returns, both
     * @return NetworkResult chứa danh sách nodes hoặc error message
     */
    suspend fun getNodesByOwnerAndType(
        owner: String,
        nodeType: String
    ): NetworkResult<List<Node>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getNodesByOwnerAndType(owner, nodeType)
                
                if (response.isSuccessful && response.body() != null) {
                    val nodes = response.body()!!
                    NetworkResult.Success(nodes)
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Không có quyền truy cập"
                        404 -> "Không tìm thấy nodes"
                        500 -> "Lỗi máy chủ, vui lòng thử lại sau"
                        else -> "Lỗi khi lấy danh sách nodes. Mã lỗi: ${response.code()}"
                    }
                    NetworkResult.Error(errorMessage)
                }
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error("Kết nối timeout. Vui lòng kiểm tra mạng và thử lại")
            } catch (e: IOException) {
                NetworkResult.Error("Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại")
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Không có quyền truy cập"
                    404 -> "Không tìm thấy nodes"
                    500 -> "Lỗi máy chủ, vui lòng thử lại sau"
                    else -> "Lỗi khi lấy danh sách nodes. Mã lỗi: ${e.code()}"
                }
                NetworkResult.Error(errorMessage)
            } catch (e: Exception) {
                NetworkResult.Error("Đã xảy ra lỗi không xác định: ${e.message}")
            }
        }
    }
    
    /**
     * Lấy tất cả nodes cho một owner (3 loại: supply, returns, both)
     * @param owner Username của người dùng
     * @return NetworkResult chứa map với key là node type và value là danh sách nodes
     */
    suspend fun getAllNodesByOwner(owner: String): NetworkResult<Map<String, List<Node>>> {
        return withContext(Dispatchers.IO) {
            try {
                val nodeTypes = listOf("supply", "returns", "both")
                val resultMap = mutableMapOf<String, List<Node>>()

                nodeTypes.forEach { nodeType ->
                    val result = getNodesByOwnerAndType(owner, nodeType)
                    when (result) {
                        is NetworkResult.Success -> {
                            resultMap[nodeType] = result.data
                        }
                        is NetworkResult.Error -> {
                            // Log error nhưng vẫn tiếp tục với các node type khác
                            resultMap[nodeType] = emptyList()
                        }
                        is NetworkResult.Loading -> {
                            // Không làm gì
                        }
                    }
                }

                NetworkResult.Success(resultMap)
            } catch (e: Exception) {
                NetworkResult.Error("Đã xảy ra lỗi khi lấy tất cả nodes: ${e.message}")
            }
        }
    }

    /**
     * Gửi lệnh đến server RCS
     * @param node_name, node_type, owner, start, end, next_start, next_end
     * @return Thông số gửi lệnh trả về
     */
    suspend fun sendRcsCommand(payload: Payload): NetworkResult<JsonElement> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.sendRcsCommand(payload)

                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Dữ liệu không hợp lệ"
                        401 -> "Không có quyền truy cập"
                        500 -> "Lỗi máy chủ, vui lòng thử lại sau"
                        else -> "Lỗi khi gửi lệnh. Mã lỗi: ${response.code()}"
                    }
                    NetworkResult.Error(errorMessage)
                }
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error("Kết nối timeout. Vui lòng kiểm tra mạng và thử lại")
            } catch (e: IOException) {
                NetworkResult.Error("Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại")
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    400 -> "Dữ liệu không hợp lệ"
                    401 -> "Không có quyền truy cập"
                    500 -> "Lỗi máy chủ, vui lòng thử lại sau"
                    else -> "Lỗi khi gửi lệnh. Mã lỗi: ${e.code()}"
                }
                NetworkResult.Error(errorMessage)
            } catch (e: Exception) {
                NetworkResult.Error("Đã xảy ra lỗi không xác định: ${e.message}")
            }
        }
    }

}

