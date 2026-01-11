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

// ------------------ Iron Man 主题色值 ------------------
object DashboardColors {
    val Primary = Color(0xFFB71C1C)
    val PrimaryVariant = Color(0xFF7F0000)
    val Secondary = Color(0xFFFFD700)
    val SecondaryVariant = Color(0xFFFFC107)
    val Accent = Color(0xFF00E5FF)
    val Background = Color(0xFF0D0D0D)
    val Surface = Color(0xFF1A1A1A)
    val Error = Color(0xFFFF5252)
    val TextPrimary = Color(0xFFF5F5F5)
    val TextSecondary = Color(0xFFE0E0E0)
}

private val DarkColorScheme = darkColorScheme(
    primary = DashboardColors.Primary,
    onPrimary = Color.White,
    primaryContainer = DashboardColors.PrimaryVariant,
    secondary = DashboardColors.Secondary,
    onSecondary = Color.Black,
    secondaryContainer = DashboardColors.SecondaryVariant,
    tertiary = DashboardColors.Accent,
    onTertiary = Color.Black,
    background = DashboardColors.Background,
    surface = DashboardColors.Surface,
    onBackground = DashboardColors.TextSecondary,
    onSurface = DashboardColors.TextPrimary,
    error = DashboardColors.Error,
    onError = Color.White
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
