package com.example.honda_caller_app.ui.login

import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.honda_caller_app.ui.theme.Honda_Caller_AppTheme
import com.example.honda_caller_app.ui.utils.ResponsiveUtils
import com.example.honda_caller_app.ui.utils.ScreenType
import kotlinx.coroutines.launch
import com.example.honda_caller_app.R
import androidx.compose.ui.layout.ContentScale
import android.widget.VideoView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val screenType = ResponsiveUtils.getScreenType()
    val padding = ResponsiveUtils.getResponsivePadding()
    val fontSize = ResponsiveUtils.getResponsiveFontSize()
    val spacing = ResponsiveUtils.getResponsiveSpacing()

    LaunchedEffect(viewModel.isLoginSuccessful) {
        if (viewModel.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = padding.extraLarge,
                    vertical = padding.extraLarge
                )
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Icon
            Row(
                modifier = Modifier.padding(padding.large),
                horizontalArrangement = Arrangement.spacedBy(spacing.large),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_thado),
                    contentDescription = "Logo Thadosoft",
                    contentScale = ContentScale.Fit ,
                    modifier = Modifier.size(
                        when (screenType) {
                            ScreenType.LARGE_TABLET -> 150.dp
                            ScreenType.TABLET -> 120.dp
                            ScreenType.PHONE -> 90.dp
                        }
                    )
                )

                Image(
                    painter = painterResource(R.drawable.logo_honda),
                    contentDescription = "Logo Honda",
                    contentScale = ContentScale.Fit ,
                    modifier = Modifier.size(
                        when (screenType) {
                            ScreenType.LARGE_TABLET -> 150.dp
                            ScreenType.TABLET -> 120.dp
                            ScreenType.PHONE -> 90.dp
                        }
                    )
                )
            }


            // Title
            Text(
                text = "Honda Caller App",
                fontSize = fontSize.title,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00A7A1),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = spacing.small)
            )

            Text(
                text = "Đăng nhập để tiếp tục",
                fontSize = fontSize.large,
                color = Color(0xFF00A7A1).copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = spacing.extraLarge)
            )

            // Login Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (screenType != ScreenType.PHONE) {
                            Modifier.widthIn(max = 400.dp)
                        } else {
                            Modifier.padding(horizontal = padding.large)
                        }
                    ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(padding.large),
                    verticalArrangement = Arrangement.spacedBy(spacing.large)
                ) {
                    // Username Field
                    OutlinedTextField(
                        value = viewModel.username,
                        onValueChange = viewModel::updateUsername,
                        label = { Text("Tên đăng nhập") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00A7A1),
                            focusedLabelColor = Color.Black
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = viewModel::updatePassword,
                        label = { Text("Mật khẩu") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password"
                            )
                        },
                        trailingIcon = {
                            TextButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = if (passwordVisible) "Ẩn" else "Hiện",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00A7A1),
                            focusedLabelColor = Color.Black
                        )
                    )

                    // Error Message
                    if (viewModel.errorMessage.isNotEmpty()) {
                        Text(
                            text = viewModel.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Login Button
                    Button(
                        onClick = {
                            viewModel.login()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                when (screenType) {
                                    ScreenType.LARGE_TABLET -> 72.dp
                                    ScreenType.TABLET -> 64.dp
                                    ScreenType.PHONE -> 56.dp
                                }
                            ),
                        enabled = !viewModel.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A7A1)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(
                                    when (screenType) {
                                        ScreenType.LARGE_TABLET -> 28.dp
                                        ScreenType.TABLET -> 24.dp
                                        ScreenType.PHONE -> 20.dp
                                    }
                                )
                            )
                        } else {
                            Text(
                                text = "Đăng nhập",
                                fontSize = fontSize.large,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = padding.extraLarge,
                    vertical = padding.extraLarge
                )
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Video chiếm 50% chiều cao
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f), // ✅ chỉ chiếm 70%
                contentAlignment = Alignment.Center
            ) {
                VideoPlayer(
                    videoUri = "android.resource://com.example.honda_caller_app/${R.raw.video_agv}",
                    modifier = Modifier.matchParentSize()
                )
            }

            // Phần tử khác ở dưới
            // Video chiếm 50% chiều cao
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(), // ✅ chỉ chiếm 70%
                contentAlignment = Alignment.Center
            ) {
                VideoPlayer(
                    videoUri = "android.resource://com.example.honda_caller_app/${R.raw.video_robotarm}",
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Honda_Caller_AppTheme {

    }
}

@Composable
fun VideoPlayer(
    videoUri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoPath(videoUri)
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    start()
                }
            }
        },
        modifier = modifier
    )
}
