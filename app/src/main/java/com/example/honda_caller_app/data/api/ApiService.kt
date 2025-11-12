package com.example.honda_caller_app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.google.gson.JsonElement

/**
 * API Service interface cho các API calls
 */
interface ApiService {
    
    /**
     * Đăng nhập
     * @param request LoginRequest chứa username và password
     * @return Response chứa LoginResponse với token
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    /**
     * Lấy danh sách nodes theo owner (advanced API)
     * @param owner Username của người dùng
     * @return Response chứa AdvancedNodesResponse với pt_nodes và vl_nodes
     */
    @GET("nodes/advanced/{owner}")
    suspend fun getAdvancedNodes(
        @Path("owner") owner: String
    ): Response<AdvancedNodesResponse>

    /**
     * Gửi lệnh lên RCS
     * @return Response kết quả gửi lệnh và payload gửi lên
     */
    @POST("caller/process-caller")
    suspend fun sendRcsCommand(@Body request: Payload): Response<JsonElement>
}

/**
 * Login Request model
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * Login Response model
 */
data class LoginResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val user: UserInfo
)

/**
 * User Info model
 */
data class UserInfo(
    val username: String,
    val id: String,
    val is_active: Boolean,
    val is_superuser: Boolean,
    val permissions: List<String>,
    val roles: List<String>,
    val area: Int? = null,
    val group_id: Int? = null,
    val route: List<String>? = null,
    val created_at: String? = null,
    val last_login: String? = null
)

data class Payload(
    val node_name: String,
    val node_type: String,
    val owner: String,
    val process_code: String,
    val start: Int,
    val end: Int,
    val next_start: Int,
    val next_end: Int,
    val line: String
)

/**
 * Response từ API nodes/advanced/{owner}
 */
data class AdvancedNodesResponse(
    val pt_nodes: NodesByType,
    val vl_nodes: NodesByType
)

/**
 * Nodes được nhóm theo node type (supply, returns, both, auto)
 * Key: node type (supply, returns, both, auto...)
 * Value: Map với Key là line name (Line A, Line B...) và Value là danh sách nodes
 */
typealias NodesByType = Map<String, Map<String, List<NodeOut>>>

/**
 * NodeOut model - một node trả về từ API
 */
data class NodeOut(
    val node_name: String,
    val node_type: String,
    val owner: String,
    val process_code: String,
    val start: Int,
    val end: Int,
    val next_start: Int,
    val next_end: Int,
    val line: String,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Type alias để tương thích với code hiện tại
 */
typealias Node = NodeOut

