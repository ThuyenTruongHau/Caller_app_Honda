package com.example.honda_caller_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import com.example.honda_caller_app.data.api.Node
import com.example.honda_caller_app.data.api.RetrofitManager
import com.example.honda_caller_app.data.local.TokenManager
import com.example.honda_caller_app.data.repository.AuthRepository
import com.example.honda_caller_app.data.repository.NodeRepository
import com.example.honda_caller_app.ui.home.HomeScreen
import com.example.honda_caller_app.ui.login.LoginScreen
import com.example.honda_caller_app.ui.login.LoginViewModel
import com.example.honda_caller_app.ui.theme.Honda_Caller_AppTheme
import com.example.honda_caller_app.ui.home.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Honda_Caller_AppTheme {
                HondaCallerApp()
            }
        }
    }
}

@Composable
fun HondaCallerApp() {
    val context = LocalContext.current
    
    // Initialize dependencies
    val tokenManager = remember { TokenManager(context) }
    val authRepository = remember { AuthRepository(RetrofitManager.apiService, tokenManager) }
    val nodeRepository = remember { NodeRepository(RetrofitManager.apiService) }
    val loginViewModel = remember { LoginViewModel(authRepository) }
    val homeViewModel = remember { HomeViewModel(nodeRepository) }
    
    // Check if already logged in
    var isLoggedIn by remember { mutableStateOf(tokenManager.isLoggedIn()) }
    var username by remember { mutableStateOf(tokenManager.getUsername()) }
    
    // Tự động lấy nodes khi đã đăng nhập
    LaunchedEffect(key1 = isLoggedIn, key2 = username) {
        if (isLoggedIn && username?.isNotEmpty() == true) {
            // Gọi API để lấy nodes qua HomeViewModel
            homeViewModel.fetchNodesIfNeeded(username!!)
        }
    }
    
    if (isLoggedIn) {
        HomeScreen(
            username = username,
            supplyNodes = homeViewModel.supplyNodes,
            returnsNodes = homeViewModel.returnsNodes,
            bothNodes = homeViewModel.bothNodes,
            onLogout = {
                authRepository.logout()
                isLoggedIn = false
                username = ""
                loginViewModel.resetLoginState()
                homeViewModel.resetState()
            },
            onNodeClick = { node -> homeViewModel.sendNodeCommand(node) },
            viewModel = homeViewModel
        )
    } else {
        LoginScreen(
            onLoginSuccess = {
                isLoggedIn = true
                username = tokenManager.getUsername()
            },
            viewModel = loginViewModel
        )
    }
}
