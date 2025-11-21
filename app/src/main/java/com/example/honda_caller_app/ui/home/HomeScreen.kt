package com.example.honda_caller_app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.honda_caller_app.data.api.Node
import com.example.honda_caller_app.ui.utils.ResponsiveUtils
import com.example.honda_caller_app.ui.utils.ScreenType
import com.google.gson.JsonElement
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource

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
    // Giữ cách reset state ban đầu như bạn đã làm
    LaunchedEffect(Unit) { viewModel.resetState() }

    val displayUsername = username ?: "User"
    val screenType = ResponsiveUtils.getScreenType()
    val padding = ResponsiveUtils.getResponsivePadding()
    val fontSize = ResponsiveUtils.getResponsiveFontSize()
    val spacing = ResponsiveUtils.getResponsiveSpacing()

    // State UI cục bộ
    var selectedCycleType by remember { mutableStateOf("Cấp") }
    var selectedLine by remember { mutableStateOf("Line 1") }
    var isSwitchEnabled by remember { mutableStateOf(false) }
    var isPhuTungMode by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedNode by remember { mutableStateOf<Node?>(null) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    // Quan sát vòng đời để dọn các lớp phủ/đối thoại khi app rơi vào nền
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // Khi tắt app → dừng hiển thị
                    viewModel.dismissNotification()
                    viewModel.dismissResultDialog()
                    showConfirmDialog = false
                    showNotificationDialog = false
                }
                Lifecycle.Event.ON_START -> {
                    // Khi quay lại app → tiếp tục queue
                    viewModel.resumeNotificationQueue()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    // Tên line khả dụng theo chế độ
    val availableLineNames = if (isSwitchEnabled) {
        viewModel.availableLineNames
    } else if (isPhuTungMode) {
        viewModel.availableLineForVLManual
    } else {
        emptyList()
    }

    // Auto chọn line đầu tiên khi sang chế độ tự động
    LaunchedEffect(isSwitchEnabled, availableLineNames) {
        if (isSwitchEnabled && availableLineNames.isNotEmpty()) {
            if (selectedLine !in availableLineNames) {
                selectedLine = availableLineNames.first()
            }
        }
    }

    // Danh sách nodes theo trạng thái hiện tại
    val currentNodes = if (isSwitchEnabled && availableLineNames.isNotEmpty()) {
        viewModel.getNodesByLine(selectedLine).map { it as Node }
    } else if (isPhuTungMode) {
        viewModel.getManualNodesReturn(selectedLine).map { it as Node }
    } else {
        when (selectedCycleType) {
            "Cấp" -> supplyNodes
            "Trả" -> returnsNodes
            "Cấp&Trả" -> bothNodes
            else -> emptyList()
        }
    }

    // ---------- Root layout: Box bọc Scaffold + overlay banner ----------
    Box(Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.height(78.dp),
                    title = {
                        Row {
                            Text(
                                text = "Honda Caller App",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x26000000)),
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text(
                                    text = "Xin chào, $displayUsername",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    },
                    actions = {
                        Row(
                            modifier = Modifier.padding(bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                shape = RoundedCornerShape(5.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x26000000)),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Logout",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Đăng xuất", fontSize = 15.sp, color = Color.White)
                            }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = padding.small, vertical = padding.small)
            ) {
                // ----- Row trên -----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.63f),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    // Cột trái (bộ lọc/chế độ)
                    Column(
                        modifier = Modifier
                            .weight(0.2f)
                            .height(416.dp),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5FEFD)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(padding.small),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Thủ công / Tự động",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(spacing.small))

                                Switch(
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = 1.2f
                                        scaleY = 1f
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

                                if (!isSwitchEnabled) {
                                    // Toggle "Lấy phụ tùng" / "Cấp vật liệu"
                                    Button(
                                        onClick = { isPhuTungMode = !isPhuTungMode },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isPhuTungMode)
                                                Color(0xFFDC143C) else Color(0xFF00A7A1)
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

                                    if (!isPhuTungMode) {
                                        Text(
                                            text = "Loại chu trình: ",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )

                                        Spacer(modifier = Modifier.height(spacing.small))

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
                                                            Modifier.border(2.dp, Color(0xFF00A7A1), RoundedCornerShape(4.dp))
                                                        } else Modifier
                                                    ),
                                                shape = RoundedCornerShape(4.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isCapSelected) Color.White else Color(0xFF00A7A1)
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
                                                            Modifier.border(2.dp, Color(0xFF00A7A1), RoundedCornerShape(4.dp))
                                                        } else Modifier
                                                    ),
                                                shape = RoundedCornerShape(4.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isTraSelected) Color.White else Color(0xFF00A7A1)
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
                                                            Modifier.border(2.dp, Color(0xFF00A7A1), RoundedCornerShape(4.dp))
                                                        } else Modifier
                                                    ),
                                                shape = RoundedCornerShape(4.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isCaHaiSelected) Color.White else Color(0xFF00A7A1)
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
                        }
                    }

                    // Cột phải (nội dung chính)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        // Header Line
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5FEFD)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(padding.small)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Line:",
                                        fontSize = fontSize.medium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )

                                    val linesToShow = availableLineNames
                                    linesToShow.forEach { lineName ->
                                        val isLineSelected = selectedLine == lineName
                                        Button(
                                            onClick = { selectedLine = lineName },
                                            modifier = Modifier
                                                .weight(1f)
                                                .then(
                                                    if (isLineSelected) {
                                                        Modifier.border(2.dp, Color(0xFF00A7A1), RoundedCornerShape(4.dp))
                                                    } else Modifier
                                                ),
                                            shape = RoundedCornerShape(4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isLineSelected) Color.White else Color(0xFF00A7A1)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                lineName,
                                                fontSize = 14.sp,
                                                color = if (isLineSelected) Color(0xFF00A7A1) else Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Danh sách nodes
                        if (currentNodes.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5FEFD)),
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
                            val columns = when (screenType) {
                                ScreenType.LARGE_TABLET -> 6
                                ScreenType.TABLET -> 4
                                ScreenType.PHONE -> 3
                            }
                            val nodeRows = currentNodes.chunked(columns)
                            val scrollState = rememberScrollState()

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(scrollState),
                                    verticalArrangement = Arrangement.spacedBy(spacing.medium)
                                ) {
                                    nodeRows.forEach { rowNodes ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(spacing.small)
                                        ) {
                                            rowNodes.forEach { node ->
                                                NodeButton(
                                                    node = node,
                                                    modifier = Modifier.weight(1f),
                                                    onClick = {
                                                        selectedNode = node
                                                        showConfirmDialog = true
                                                    }
                                                )
                                            }
                                            repeat(columns - rowNodes.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }

                                // Thanh cuộn dọc custom
                                val maxScroll = scrollState.maxValue
                                if (maxScroll > 0) {
                                    val density = LocalDensity.current
                                    val viewportHeightPx = scrollState.viewportSize
                                    val viewportHeight = with(density) { viewportHeightPx.toDp() }
                                    val contentHeight = maxScroll + viewportHeightPx
                                    val scrollbarThumbHeightPx =
                                        (viewportHeightPx.toFloat() / contentHeight.toFloat() * viewportHeightPx.toFloat())
                                            .coerceAtLeast(20f)
                                    val scrollbarThumbHeight = with(density) { scrollbarThumbHeightPx.toDp() }
                                    val scrollbarOffset = if (maxScroll > 0 && viewportHeightPx > 0) {
                                        val maxOffsetPx = viewportHeightPx - scrollbarThumbHeightPx
                                        val offsetPx = (scrollState.value.toFloat() / maxScroll.toFloat() * maxOffsetPx)
                                        with(density) { offsetPx.toDp() }
                                    } else 0.dp

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .height(viewportHeight)
                                            .width(6.dp),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .offset(y = scrollbarOffset)
                                                .height(scrollbarThumbHeight)
                                                .fillMaxWidth()
                                                .background(
                                                    color = Color.Gray.copy(alpha = 0.6f),
                                                    shape = RoundedCornerShape(3.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ----- Row dưới: bảng 5 cột -----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(top = padding.small),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5FEFD)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFF00A7A1),
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                                    .padding(horizontal = padding.small, vertical = padding.small),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("STT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Text("Line", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Text("Nhiệm vụ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Text("Thời gian", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Text("Trạng Thái", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                repeat(10) { index ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = if (index % 2 == 0) Color.White else Color(0xFFF9F9F9)
                                            )
                                            .padding(horizontal = padding.medium, vertical = padding.small),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Dữ liệu ${index + 1}-1", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Text("Dữ liệu ${index + 1}-2", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Text("Dữ liệu ${index + 1}-3", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Text("Dữ liệu ${index + 1}-4", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Text("Dữ liệu ${index + 1}-5", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    }
                                    if (index < 9) Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ----- Overlay banner kiểu Messenger (an toàn, không Popup) -----
        val active = viewModel.currentNotification
        if (viewModel.showNotification && active != null) {
            TopNotificationBanner(
                title = active.title,
                message = active.message,
                timestamp = active.timestamp,
                topBarHeight = 78.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .zIndex(100f),
                onClick = {
                    showNotificationDialog = true
                    viewModel.onNotificationDismissed()
                },
                onDismiss = {
                    viewModel.onNotificationDismissed()
                }
            )
        }

        // ----- Dialog: Confirm / Result / Danh sách thông báo -----
        if (showConfirmDialog && selectedNode != null) {
            NodeConfirmDialog(
                node = selectedNode!!,
                onConfirm = {
                    selectedNode?.let { node ->
                        onNodeClick(node)
                        showConfirmDialog = false
                        selectedNode = null
                    }
                },
                onDismiss = {
                    showConfirmDialog = false
                    selectedNode = null
                }
            )
        }

        if (viewModel.showResultDialog) {
            ResultDialog(
                isSuccess = viewModel.commandResult == "Thành công",
                message = viewModel.commandResult,
                data = viewModel.commandData,
                onDismiss = { viewModel.dismissResultDialog() }
            )
        }

        if (showNotificationDialog) {
            NotificationListDialog(
                notifications = viewModel.notifications,
                onDismiss = { showNotificationDialog = false },
                onClearAll = { viewModel.clearAllNotifications() },
                onRemoveNotification = { id -> viewModel.removeNotification(id) }
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
                ScreenType.LARGE_TABLET -> 90.dp
                ScreenType.TABLET -> 70.dp
                ScreenType.PHONE -> 60.dp
            }
        ),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC143C)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = node.node_name,
                fontSize = fontSize.medium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "${node.start} -> ${node.end}",
                fontSize = 10.sp,
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5FEFD)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Tên Node: ${node.node_name}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("Loại: ${node.node_type}", fontSize = 14.sp, color = Color.Gray)
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Điểm START", fontSize = 12.sp, color = Color.Gray)
                                Text("${node.start}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00A7A1))
                            }
                            Text("→", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00A7A1), modifier = Modifier.padding(horizontal = 16.dp))
                            Column {
                                Text("Điểm END", fontSize = 12.sp, color = Color.Gray)
                                Text("${node.end}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00A7A1))
                            }
                        }
                    }
                }
                Text("Bạn có chắc chắn muốn gửi lệnh này?", fontSize = 14.sp, color = Color.Black)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A7A1))
            ) { Text("Xác nhận", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy", color = Color.Gray) } },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Banner thông báo an toàn (không Popup). Rộng 60% màn hình, cao bằng TopAppBar.
 */
@Composable
fun TopNotificationBanner(
    title: String,
    message: String,
    timestamp: String? = null,
    topBarHeight: Dp = 78.dp,
    autoDismissMillis: Long = 4000L,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val bannerWidth = LocalConfiguration.current.screenWidthDp.dp * 0.6f
    var visible by remember { mutableStateOf(false) }

    // Hiện -> đợi autoDismiss -> ẩn
    LaunchedEffect(title, message, timestamp) {
        visible = true
        delay(autoDismissMillis)
        visible = false
    }
    // Khi ẩn xong -> báo ViewModel đóng state
    LaunchedEffect(visible) {
        if (!visible) onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(200)),
        exit  = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(200))
    ) {
        Card(
            modifier = Modifier
                .width(bannerWidth)
                .height(topBarHeight)
                .shadow(8.dp, shape = RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFFF9800))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = message,
                            fontSize = 13.sp,
                            color = Color(0xFF374151),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (timestamp != null) {
                            Text(text = "• $timestamp", fontSize = 12.sp, color = Color(0xFF6B7280), maxLines = 1)
                        }
                    }
                }
                IconButton(onClick = { visible = false }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = Color(0xFF6B7280))
                }
            }
        }
    }
}

@Composable
fun NotificationDialog(
    title: String,
    message: String,
    timestamp: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                    if (timestamp != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = timestamp, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Normal)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A7A1))) {
                Text("Đóng", color = Color.White)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun NotificationListDialog(
    notifications: List<NotificationItem>,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit,
    onRemoveNotification: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Thông báo (${notifications.size})", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                }
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAll) { Text("Xóa tất cả", fontSize = 12.sp, color = Color(0xFFDC143C)) }
                }
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) { Text(text = "Không có thông báo nào", fontSize = 14.sp, color = Color.Gray) }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    notifications.forEach { notification ->
                        NotificationItemCard(notification = notification, onRemove = { onRemoveNotification(notification.id) })
                        if (notification != notifications.last()) Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A7A1))) {
                Text("Đóng", color = Color.White)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun NotificationItemCard(
    notification: NotificationItem,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5FEFD)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = notification.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                if (notification.timestamp != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = notification.timestamp, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Normal)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onRemove, contentPadding = PaddingValues(0.dp)) {
                    Text(text = "Xóa", fontSize = 12.sp, color = Color(0xFFDC143C))
                }
            }
        }
    }
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
                    Text("✓ Thành công", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF00A7A1))
                } else {
                    Text("✗ Thất bại", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFDC143C))
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
                if (isSuccess && data != null) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5FEFD)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Dữ liệu trả về:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(data.toString(), fontSize = 12.sp, color = Color.Black, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
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
            ) { Text("Đóng", color = Color.White) }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
