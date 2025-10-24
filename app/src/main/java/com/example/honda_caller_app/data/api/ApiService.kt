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
     * Lấy danh sách nodes theo owner và node type
     * @param owner Username của người dùng
     * @param nodeType Loại node: supply, returns, both
     * @return Response chứa danh sách nodes
     */
    @GET("nodes/owner/{owner}/{node_type}")
    suspend fun getNodesByOwnerAndType(
        @Path("owner") owner: String,
        @Path("node_type") nodeType: String
    ): Response<List<Node>>

    /**
     * Lấy danh sách nodes theo owner và node type
     * @param owner Username của người dùng
     * @param nodeType Loại node: supply, returns, both
     * @return Response chứa danh sách nodes
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
    val supply: String? = null,
    val returns: String? = null,
    val both: String? = null,
    val created_at: String? = null,
    val last_login: String? = null
)

/**
 * Node List Response model
 */
data class NodeListResponse(
    val nodes: List<Node>
)

/**
 * Node model
 */
data class Node(
    val id: String,
    val node_name: String,
    val node_type: String,
    val owner: String,
    val start: Int,
    val end: Int,
    val next_start: Int,
    val next_end: Int,
    val created_at: String,
    val updated_at: String
)

data class Payload(
    val node_name: String,
    val node_type: String,
    val owner: String,
    val start: Int,
    val end: Int,
    val next_start: Int,
    val next_end: Int,
)

