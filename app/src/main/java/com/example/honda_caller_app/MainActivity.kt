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
import com.example.honda_caller_app.data.network.WebSocketManager
import com.example.honda_caller_app.data.repository.SocketRepository


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

    // Check if already logged in
    var isLoggedIn by remember { mutableStateOf(tokenManager.isLoggedIn()) }
    var username by remember { mutableStateOf(tokenManager.getUsername()) }
    var route by remember { mutableStateOf(tokenManager.getRoute()) }
    val TasksManager = remember {
        WebSocketManager(
            client = RetrofitManager.okHttpClient // hoặc RetrofitManager.okHttpClient
        )
    }
    val webSocketRepository = remember {
        SocketRepository(TasksManager)
    }
    val homeViewModel = remember {
        HomeViewModel(
            nodeRepository = nodeRepository,
            webSocketRepository = webSocketRepository // Truyền repository vào ViewModel
        )
    }
    
    // Tự động lấy nodes khi đã đăng nhập
    LaunchedEffect(key1 = isLoggedIn, key2 = username) {
        if (isLoggedIn && username?.isNotEmpty() == true) {
            // Gọi API để lấy nodes qua HomeViewModel
            homeViewModel.fetchNodesIfNeeded(username!!)
        }
    }

    LaunchedEffect(key1 = isLoggedIn) {
        if (isLoggedIn) {
            // Đọc trực tiếp từ tokenManager mỗi lần LaunchedEffect chạy
            val groupId = tokenManager.getGroupId()
            if (groupId != null && groupId.isNotEmpty()) {
                TasksManager.connect("notification/$groupId")
            }
        } else {
            // Ngắt kết nối nếu đã logout
            TasksManager.disconnect()
        }
    }
    
    if (isLoggedIn) {
        HomeScreen(
            username = username,
            supplyNodes = homeViewModel.supplyNodes,
            returnsNodes = homeViewModel.returnsNodes,
            bothNodes = homeViewModel.bothNodes,
            onLogout = {
                webSocketRepository.disconnect()
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
                route = tokenManager.getRoute()
            },
            viewModel = loginViewModel
        )
    }
}
