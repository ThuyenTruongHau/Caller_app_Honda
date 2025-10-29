package com.example.honda_caller_app.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.honda_caller_app.data.api.Node
import com.example.honda_caller_app.ui.theme.Honda_Caller_AppTheme
import com.example.honda_caller_app.ui.utils.ResponsiveUtils
import com.example.honda_caller_app.ui.utils.ScreenType
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import com.google.gson.JsonElement
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String? = null,
    supplyNodes: List<Node> = emptyList(),
    returnsNodes: List<Node> = emptyList(),
    bothNodes: List<Node> = emptyList(),
    onLogout: () -> Unit,
    onNodeClick: (Node) -> Unit,
    viewModel: HomeViewModel
) {
    val displayUsername = username ?: "User"
    val screenType = ResponsiveUtils.getScreenType()
    val padding = ResponsiveUtils.getResponsivePadding()
    val fontSize = ResponsiveUtils.getResponsiveFontSize()
    val spacing = ResponsiveUtils.getResponsiveSpacing()

    // State để lưu loại chu trình được chọn (Cấp, Trả, Cấp&Trả) - dùng cho nút bên trái
    var selectedCycleType by remember { mutableStateOf("Cấp") }
    
    // State để lưu Line được chọn (Line 1, Line 2, Line 3) - dùng cho nút bên phải
    var selectedLine by remember { mutableStateOf("Line 1") }

    // State để lưu switch
    var isSwitchEnabled by remember { mutableStateOf(false) }
    
    // State để toggle nút "Lấy phụ tùng" / "Cấp vật liệu"
    var isPhuTungMode by remember { mutableStateOf(false) }

    // State để hiển thị confirm dialog
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedNode by remember { mutableStateOf<Node?>(null) }
    
    // State để hiển thị dialog thông báo
    var showNotificationDialog by remember { mutableStateOf(false) }

    // Lấy danh sách nodes tương ứng với loại chu trình được chọn
    val currentNodes = when (selectedCycleType) {
        "Cấp" -> supplyNodes
        "Trả" -> returnsNodes
        "Cấp&Trả" -> bothNodes
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(130.dp), // Tăng height của TopAppBar
                title = {
                    Column(
                        modifier = Modifier.padding(bottom = 6.dp) // Thêm padding dưới cùng
                    ) {
                        // Dòng 1: tiêu đề
                        Text(
                            text = "Honda Caller App",
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp // Tăng size font cho tiêu đề
                        )

                        // Dòng 2: card nhỏ hiển thị user
                        Card(
                            modifier = Modifier
                                .padding(top = 6.dp), // Tăng khoảng cách giữa tiêu đề và card
                            colors = CardDefaults.cardColors(containerColor = Color(0x26000000)), // hơi trong suốt
                            shape = RoundedCornerShape(4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "Xin chào, $displayUsername",
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ), // Tăng padding
                                color = Color.White,
                                fontSize = 15.sp // Tăng size font
                            )
                        }
                    }

                },
                actions = {
                    // Icon chuông cảnh báo
                    IconButton(
                        onClick = { showNotificationDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Thông báo",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .sizeIn(minWidth = 100.dp, minHeight = 40.dp)
                            .padding(
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            ), // Khoảng cách với viền
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x26000000)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Đăng xuất", fontSize = 16.sp, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00A7A1),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    horizontal = padding.large,
                    vertical = padding.large
                ),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            // Cột bên trái
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Welcome Card - Chọn lọc
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5FEFD)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(padding.large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Thủ công / Tự động",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(spacing.small))

                        Switch(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = 1.5f  // Scale width (2 lần)
                                    scaleY = 1.2f  // Giữ nguyên height
                                },
                            checked = isSwitchEnabled,
                            onCheckedChange = { isSwitchEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00A7A1),
                                uncheckedThumbColor = Color(0xFFCCCCCC),
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )

                        Spacer(modifier = Modifier.height(spacing.medium))

                        // Nút toggle "Lấy phụ tùng" / "Cấp vật liệu"
                        Button(
                            onClick = { isPhuTungMode = !isPhuTungMode },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPhuTungMode) Color(0xFFDC143C) else Color(
                                    0xFF00A7A1
                                )
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                if (isPhuTungMode) "Cấp vật liệu" else "Lấy phụ tùng",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.medium))

                        Text(
                            text = "Loại chu trình: ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(spacing.small))

                        // Button chu trình - 3 nút với width full và height bằng nhau
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(spacing.small)
                        ) {
                            val isCapSelected = selectedCycleType == "Cấp"
                            Button(
                                onClick = { selectedCycleType = "Cấp" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .then(
                                        if (isCapSelected) {
                                            Modifier.border(
                                                2.dp,
                                                Color(0xFF00A7A1),
                                                RoundedCornerShape(4.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCapSelected) Color.White else Color(
                                        0xFF00A7A1
                                    )
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Cấp xe",
                                    fontSize = 14.sp,
                                    color = if (isCapSelected) Color(0xFF00A7A1) else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val isTraSelected = selectedCycleType == "Trả"
                            Button(
                                onClick = { selectedCycleType = "Trả" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .then(
                                        if (isTraSelected) {
                                            Modifier.border(
                                                2.dp,
                                                Color(0xFF00A7A1),
                                                RoundedCornerShape(4.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTraSelected) Color.White else Color(
                                        0xFF00A7A1
                                    )
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Trả trống",
                                    fontSize = 14.sp,
                                    color = if (isTraSelected) Color(0xFF00A7A1) else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val isCaHaiSelected = selectedCycleType == "Cấp&Trả"
                            Button(
                                onClick = { selectedCycleType = "Cấp&Trả" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .then(
                                        if (isCaHaiSelected) {
                                            Modifier.border(
                                                2.dp,
                                                Color(0xFF00A7A1),
                                                RoundedCornerShape(4.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCaHaiSelected) Color.White else Color(
                                        0xFF00A7A1
                                    )
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Cấp xe & trả trống",
                                    fontSize = 14.sp,   
                                    color = if (isCaHaiSelected) Color(0xFF00A7A1) else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }


            // Cột bên phải
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(spacing.large)
            ) {
                // Welcome Card - Chọn loại chu trình
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5FEFD)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(padding.small)
                    ) {
                        // Row chứa text và buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Line:",
                                fontSize = fontSize.large,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            // Button Line
                            val isLine1Selected = selectedLine == "Line 1"
                            Button(
                                onClick = { selectedLine = "Line 1" },
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (isLine1Selected) {
                                            Modifier.border(
                                                2.dp,
                                                Color(0xFF00A7A1),
                                                RoundedCornerShape(4.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLine1Selected) Color.White else Color(
                                        0xFF00A7A1
                                    )
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Line 1",
                                    fontSize = 14.sp,
                                    color = if (isLine1Selected) Color(0xFF00A7A1) else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val isLine2Selected = selectedLine == "Line 2"
                            Button(
                                onClick = { selectedLine = "Line 2" },
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (isLine2Selected) {
                                            Modifier.border(
                                                2.dp,
                                                Color(0xFF00A7A1),
                                                RoundedCornerShape(4.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLine2Selected) Color.White else Color(
                                        0xFF00A7A1
                                    )
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Line 2",
                                    fontSize = 14.sp,
                                    color = if (isLine2Selected) Color(0xFF00A7A1) else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val isLine3Selected = selectedLine == "Line 3"
                            Button(
                                onClick = { selectedLine = "Line 3" },
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (isLine3Selected) {
                                            Modifier.border(
                                                2.dp,
                                                Color(0xFF00A7A1),
                                                RoundedCornerShape(4.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLine3Selected) Color.White else Color(
                                        0xFF00A7A1
                                    )
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Line 3",
                                    fontSize = 14.sp,
                                    color = if (isLine3Selected) Color(0xFF00A7A1) else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Hiển thị danh sách nodes
                if (currentNodes.isEmpty()) {
                    // Nếu không có nodes nào
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5FEFD)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không có nodes nào",
                                fontSize = fontSize.medium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Hiển thị nodes trong grid
                    val columns = when (screenType) {
                        ScreenType.LARGE_TABLET -> 4
                        ScreenType.TABLET -> 3
                        ScreenType.PHONE -> 2
                    }

                    // Tách nodes thành các nhóm theo số cột
                    val nodeRows = currentNodes.chunked(columns)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        nodeRows.forEach { rowNodes ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                            ) {
                                rowNodes.forEach { node ->
                                    NodeButton(
                                        node = node,
                                        modifier = Modifier.weight(1f),
                                        onClick = {  // DÙNG INLINE LAMBDA
                                            selectedNode = node
                                            showConfirmDialog = true
                                        }
                                    )
                                }
                                // Thêm các button trống để fill hàng
                                repeat(columns - rowNodes.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Confirm Dialog
        if (showConfirmDialog && selectedNode != null) {
            NodeConfirmDialog(
                node = selectedNode!!,
                onConfirm = {  // DÙNG INLINE LAMBDA
                    selectedNode?.let { node ->
                        onNodeClick(node)
                        showConfirmDialog = false
                        selectedNode = null
                    }
                },
                onDismiss = {  // DÙNG INLINE LAMBDA
                    showConfirmDialog = false
                    selectedNode = null
                }
            )
        }

        // Result Dialog - THÊM PHẦN NÀY
        if (viewModel.showResultDialog) {
            ResultDialog(
                isSuccess = viewModel.commandResult == "Thành công",
                message = viewModel.commandResult,
                data = viewModel.commandData,
                onDismiss = { viewModel.dismissResultDialog() }
            )
        }

        // Notification Dialog
        if (showNotificationDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thông báo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Đây là khu vực thông báo cảnh báo.",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Các thông báo quan trọng sẽ được hiển thị tại đây.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showNotificationDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A7A1)
                        )
                    ) {
                        Text("Đóng", color = Color.White)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun NodeButton(
    node: Node,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val screenType = ResponsiveUtils.getScreenType()
    val fontSize = ResponsiveUtils.getResponsiveFontSize()

    Button(
        onClick = onClick,
        modifier = modifier.height(
            when (screenType) {
                ScreenType.LARGE_TABLET -> 130.dp
                ScreenType.TABLET -> 110.dp
                ScreenType.PHONE -> 100.dp
            }
        ),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC143C)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = node.node_name,
                fontSize = fontSize.large,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${node.start} -> ${node.end}",
                fontSize = fontSize.small,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NodeConfirmDialog(
    node: Node,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Xác nhận gửi lệnh",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thông tin node
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5FEFD)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tên Node: ${node.node_name}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Loại: ${node.node_type}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // Hiển thị điểm start và end
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Điểm START",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${node.start}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00A7A1)
                                )
                            }

                            Text(
                                text = "→",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00A7A1),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Column {
                                Text(
                                    text = "Điểm END",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${node.end}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00A7A1)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Bạn có chắc chắn muốn gửi lệnh này?",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00A7A1)
                )
            ) {
                Text("Xác nhận", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ResultDialog(
    isSuccess: Boolean,
    message: String,
    data: JsonElement?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSuccess) {
                    Text(
                        text = "✓ Thành công",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF00A7A1)
                    )
                } else {
                    Text(
                        text = "✗ Thất bại",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFFDC143C)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = if (isSuccess) Color(0xFF00A7A1) else Color(0xFFDC143C)
                )

                // Hiển thị data nếu thành công
                if (isSuccess && data != null) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5FEFD)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Dữ liệu trả về:",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = data.toString(),
                                fontSize = 12.sp,
                                color = Color.Black,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSuccess) Color(0xFF00A7A1) else Color(0xFFDC143C)
                )
            ) {
                Text("Đóng", color = Color.White)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

//@Preview(showBackground = true)
//@Composable
//fun HomeScreenPreview() {
//    Honda_Caller_AppTheme {
//        HomeScreen(
//            username = "admin",
//            supplyNodes = emptyList(),
//            returnsNodes = emptyList(),
//            bothNodes = emptyList(),
//            onLogout = {},
//            onNodeClick = {},
//            viewModel = HomeViewModel() // Pass a dummy viewModel for preview
//        )
//    }
//}
