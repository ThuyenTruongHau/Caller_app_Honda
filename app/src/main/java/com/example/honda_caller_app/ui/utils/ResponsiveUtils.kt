package com.example.honda_caller_app.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

/**
 * Utility class for responsive design
 * Helps determine screen size and provides appropriate dimensions
 */
object ResponsiveUtils {
    
    @Composable
    fun isTablet(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp >= 600
    }
    
    @Composable
    fun isLargeTablet(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp >= 900
    }
    
    @Composable
    fun getScreenType(): ScreenType {
        val configuration = LocalConfiguration.current
        return when {
            configuration.screenWidthDp >= 900 -> ScreenType.LARGE_TABLET
            configuration.screenWidthDp >= 600 -> ScreenType.TABLET
            else -> ScreenType.PHONE
        }
    }
    
    @Composable
    fun getResponsivePadding(): ResponsivePadding {
        val screenType = getScreenType()
        return when (screenType) {
            ScreenType.LARGE_TABLET -> ResponsivePadding(
                small = 12.dp,
                medium = 20.dp,
                large = 32.dp,
                extraLarge = 48.dp
            )
            ScreenType.TABLET -> ResponsivePadding(
                small = 8.dp,
                medium = 16.dp,
                large = 24.dp,
                extraLarge = 40.dp
            )
            ScreenType.PHONE -> ResponsivePadding(
                small = 4.dp,
                medium = 8.dp,
                large = 16.dp,
                extraLarge = 24.dp
            )
        }
    }
    
    @Composable
    fun getResponsiveFontSize(): ResponsiveFontSize {
        val screenType = getScreenType()
        return when (screenType) {
            ScreenType.LARGE_TABLET -> ResponsiveFontSize(
                small = 16.sp,
                medium = 18.sp,
                large = 22.sp,
                extraLarge = 26.sp,
                title = 36.sp
            )
            ScreenType.TABLET -> ResponsiveFontSize(
                small = 14.sp,
                medium = 16.sp,
                large = 20.sp,
                extraLarge = 24.sp,
                title = 32.sp
            )
            ScreenType.PHONE -> ResponsiveFontSize(
                small = 10.sp,
                medium = 12.sp,
                large = 16.sp,
                extraLarge = 20.sp,
                title = 24.sp
            )
        }
    }
    
    @Composable
    fun getResponsiveSpacing(): ResponsiveSpacing {
        val screenType = getScreenType()
        return when (screenType) {
            ScreenType.LARGE_TABLET -> ResponsiveSpacing(
                small = 12.dp,
                medium = 20.dp,
                large = 32.dp,
                extraLarge = 48.dp
            )
            ScreenType.TABLET -> ResponsiveSpacing(
                small = 8.dp,
                medium = 16.dp,
                large = 24.dp,
                extraLarge = 40.dp
            )
            ScreenType.PHONE -> ResponsiveSpacing(
                small = 4.dp,
                medium = 8.dp,
                large = 16.dp,
                extraLarge = 24.dp
            )
        }
    }
}

enum class ScreenType {
    PHONE,
    TABLET,
    LARGE_TABLET
}

data class ResponsivePadding(
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp
)

data class ResponsiveFontSize(
    val small: TextUnit,
    val medium: TextUnit,
    val large: TextUnit,
    val extraLarge: TextUnit,
    val title: TextUnit
)

data class ResponsiveSpacing(
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp
)
