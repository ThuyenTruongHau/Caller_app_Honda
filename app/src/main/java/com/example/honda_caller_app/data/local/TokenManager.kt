package com.example.honda_caller_app.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * TokenManager để quản lý việc lưu trữ và lấy token
 */
class TokenManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "honda_caller_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_ROLES = "roles"
        private const val KEY_PERMISSIONS = "permissions"
        private const val KEY_IS_ACTIVE = "is_active"
        private const val KEY_IS_SUPERUSER = "is_superuser"
        private const val KEY_SUPPLY = "supply"
        private const val KEY_RETURNS = "returns"
        private const val KEY_BOTH = "both"
        private const val KEY_CREATED_AT = "created_at"
        private const val KEY_LAST_LOGIN = "last_login"
    }
    
    /**
     * Lưu access token
     */
    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }
    
    /**
     * Lấy access token
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * Lưu refresh token
     */
    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }
    
    /**
     * Lấy refresh token
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Lưu user info
     */
    fun saveUserInfo(
        userId: String,
        username: String,
        roles: List<String>? = null,
        permissions: List<String>? = null,
        isActive: Boolean? = null,
        isSuperuser: Boolean? = null,
        supply: String? = null,
        returns: String? = null,
        both: String? = null,
        createdAt: String? = null,
        lastLogin: String? = null
    ) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putStringSet(KEY_ROLES, roles?.toSet())
            .putStringSet(KEY_PERMISSIONS, permissions?.toSet())
            .putBoolean(KEY_IS_ACTIVE, isActive ?: false)
            .putBoolean(KEY_IS_SUPERUSER, isSuperuser ?: false)
            .putString(KEY_SUPPLY, supply)
            .putString(KEY_RETURNS, returns)
            .putString(KEY_BOTH, both)
            .putString(KEY_CREATED_AT, createdAt)
            .putString(KEY_LAST_LOGIN, lastLogin)
            .apply()
    }
    
    /**
     * Lấy user ID
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
    /**
     * Lấy username
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, "")
    }
    
    /**
     * Đặt trạng thái đăng nhập
     */
    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    /**
     * Kiểm tra đã đăng nhập chưa
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Lấy roles
     */
    fun getRoles(): Set<String>? {
        return prefs.getStringSet(KEY_ROLES, null)
    }
    
    /**
     * Lấy permissions
     */
    fun getPermissions(): Set<String>? {
        return prefs.getStringSet(KEY_PERMISSIONS, null)
    }
    
    /**
     * Kiểm tra is_active
     */
    fun isActive(): Boolean {
        return prefs.getBoolean(KEY_IS_ACTIVE, false)
    }
    
    /**
     * Kiểm tra is_superuser
     */
    fun isSuperuser(): Boolean {
        return prefs.getBoolean(KEY_IS_SUPERUSER, false)
    }
    
    /**
     * Lấy supply
     */
    fun getSupply(): String? {
        return prefs.getString(KEY_SUPPLY, null)
    }
    
    /**
     * Lấy returns
     */
    fun getReturns(): String? {
        return prefs.getString(KEY_RETURNS, null)
    }
    
    /**
     * Lấy both
     */
    fun getBoth(): String? {
        return prefs.getString(KEY_BOTH, null)
    }
    
    /**
     * Lấy created_at
     */
    fun getCreatedAt(): String? {
        return prefs.getString(KEY_CREATED_AT, null)
    }
    
    /**
     * Lấy last_login
     */
    fun getLastLogin(): String? {
        return prefs.getString(KEY_LAST_LOGIN, null)
    }
    
    /**
     * Xóa tất cả thông tin đăng nhập
     */
    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_ROLES)
            .remove(KEY_PERMISSIONS)
            .remove(KEY_IS_ACTIVE)
            .remove(KEY_IS_SUPERUSER)
            .remove(KEY_SUPPLY)
            .remove(KEY_RETURNS)
            .remove(KEY_BOTH)
            .remove(KEY_CREATED_AT)
            .remove(KEY_LAST_LOGIN)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
}

