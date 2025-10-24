package com.example.honda_caller_app.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.honda_caller_app.data.api.Node
import com.example.honda_caller_app.data.network.NetworkResult
import com.example.honda_caller_app.data.repository.AuthRepository
import com.example.honda_caller_app.data.repository.NodeRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var username by mutableStateOf("")
        private set
    
    var password by mutableStateOf("")
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf("")
        private set
    
    var isLoginSuccessful by mutableStateOf(false)
        private set
    
    fun updateUsername(username: String) {
        this.username = username
        clearError()
    }
    
    fun updatePassword(password: String) {
        this.password = password
        clearError()
    }
    
    fun login() {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Vui lòng nhập đầy đủ thông tin"
            return
        }
        
        isLoading = true
        errorMessage = ""
        
        viewModelScope.launch {
            when (val result = authRepository.login(username, password)) {
                is NetworkResult.Success -> {
                    isLoading = false
                    isLoginSuccessful = true
                }
                is NetworkResult.Error -> {
                    isLoading = false
                    errorMessage = result.message
                }
                is NetworkResult.Loading -> {
                    // Loading state đã được handle bởi isLoading
                }
            }
        }
    }
    
    private fun clearError() {
        if (errorMessage.isNotEmpty()) {
            errorMessage = ""
        }
    }
    
    fun resetLoginState() {
        isLoginSuccessful = false
        errorMessage = ""
    }
}
