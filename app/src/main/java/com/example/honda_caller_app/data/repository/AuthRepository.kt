package com.example.honda_caller_app.data.repository

import com.example.honda_caller_app.data.api.ApiService
import com.example.honda_caller_app.data.api.LoginRequest
import com.example.honda_caller_app.data.local.TokenManager
import com.example.honda_caller_app.data.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * AuthRepository để quản lý các API calls và logic authentication
 */
class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    
    /**
     * Đăng nhập với retry logic và error handling
     */
    suspend fun login(username: String, password: String): NetworkResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(username, password)
                
                // Retry logic - thử tối đa 3 lần
                var lastException: Exception? = null
                for (attempt in 1..3) {
                    try {
                        val response = apiService.login(request)
                        
                        if (response.isSuccessful && response.body() != null) {
                            val loginResponse = response.body()!!
                            
                            // Lưu tokens
                            tokenManager.saveAccessToken(loginResponse.access_token)
                            tokenManager.saveRefreshToken(loginResponse.refresh_token)
                            
                            // Lưu user info
                            tokenManager.saveUserInfo(
                                userId = loginResponse.user.id,
                                username = loginResponse.user.username,
                                roles = loginResponse.user.roles,
                                permissions = loginResponse.user.permissions,
                                isActive = loginResponse.user.is_active,
                                isSuperuser = loginResponse.user.is_superuser,
                                area = loginResponse.user.area,
                                group_id = loginResponse.user.group_id,
                                route = loginResponse.user.route,
                                createdAt = loginResponse.user.created_at,
                                lastLogin = loginResponse.user.last_login
                            )
                            
                            tokenManager.setLoggedIn(true)
                            
                            return@withContext NetworkResult.Success(Unit)
                        } else {
                            // HTTP error response
                            val errorMessage = when (response.code()) {
                                401 -> "Tên đăng nhập hoặc mật khẩu không đúng"
                                403 -> "Bạn không có quyền truy cập"
                                404 -> "Không tìm thấy dịch vụ"
                                500 -> "Lỗi máy chủ, vui lòng thử lại sau"
                                else -> "Đăng nhập thất bại. Mã lỗi: ${response.code()}"
                            }
                            return@withContext NetworkResult.Error(errorMessage)
                        }
                    } catch (e: SocketTimeoutException) {
                        lastException = e
                        if (attempt < 3) {
                            delay(1000L * attempt) // Exponential backoff
                        }
                    } catch (e: IOException) {
                        lastException = e
                        if (attempt < 3) {
                            delay(1000L * attempt)
                        }
                    }
                }
                
                // Nếu retry hết mà vẫn fail
                val errorMessage = when (lastException) {
                    is SocketTimeoutException -> "Kết nối timeout. Vui lòng kiểm tra mạng và thử lại"
                    is IOException -> "Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại"
                    else -> "Không thể kết nối đến server. Vui lòng thử lại sau"
                }
                NetworkResult.Error(errorMessage)
                
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Tên đăng nhập hoặc mật khẩu không đúng"
                    403 -> "Bạn không có quyền truy cập"
                    404 -> "Không tìm thấy dịch vụ"
                    500 -> "Lỗi máy chủ, vui lòng thử lại sau"
                    else -> "Đăng nhập thất bại. Mã lỗi: ${e.code()}"
                }
                NetworkResult.Error(errorMessage)
            } catch (e: Exception) {
                NetworkResult.Error("Đã xảy ra lỗi không xác định: ${e.message}")
            }
        }
    }
    
    /**
     * Đăng xuất
     */
    fun logout() {
        tokenManager.clearTokens()
    }
    
    /**
     * Kiểm tra đã đăng nhập chưa
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
    
    /**
     * Lấy access token hiện tại
     */
    fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }
}

