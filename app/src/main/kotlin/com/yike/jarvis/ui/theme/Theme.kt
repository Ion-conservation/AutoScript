package com.yike.jarvis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ------------------ Dashboard 深色主题色值 ------------------
object DashboardColors {
    val Background = Color(0xFF0F0F0F)
    val CardBackground = Color(0xFF1A1A1A)
    val BorderColor = Color(0xFF333333)
    val AccentGreen = Color(0xFF00FF00)
    val AccentPink = Color(0xFFD1336B)
    val WarningOrange = Color(0xFFFFA500)
    val InfoBlue = Color(0xFF0099FF)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF999999)
}

private val DarkColorScheme = darkColorScheme(
    primary = DashboardColors.AccentGreen,
    secondary = DashboardColors.InfoBlue,
    tertiary = DashboardColors.AccentPink,
    background = DashboardColors.Background,
    surface = DashboardColors.CardBackground,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DashboardColors.TextPrimary,
    onSurface = DashboardColors.TextPrimary,
    error = DashboardColors.AccentPink
)

val AppTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 0.5.sp,
        color = Color.White
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = Color.White
    )
)

@Composable
fun AutoScriptAppTheme(
    darkTheme: Boolean = true, // 默认强制深色，符合 Dashboard 风格
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
