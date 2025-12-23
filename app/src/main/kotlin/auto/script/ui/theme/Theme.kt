package auto.script.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ------------------ 方案一：薄荷苏打 核心色值 ------------------
val MintGreen = Color(0xFF64D2B1)      // 主色：启动、运行状态
val SkyBlue = Color(0xFF8ECAFE)        // 辅助：待办事项、图标
val CloudWhite = Color(0xFFF8FAFC)     // 背景：极浅冷灰白
val DeepRockGray = Color(0xFF334155)   // 文字：深岩灰
val SoftCoral = Color(0xFFFFADAD)      // 警告：停止、紧急
val CardWhite = Color(0xFFFFFFFF)      // 卡片：纯白

// ------------------ 定义 Light Color Scheme ------------------
private val MintSoda = lightColorScheme(
    primary = MintGreen,
    onPrimary = Color.White,
    secondary = SkyBlue,
    onSecondary = Color.White,
    background = CloudWhite,
    surface = CardWhite,
    onBackground = DeepRockGray,
    onSurface = DeepRockGray,
    error = SoftCoral
)

// ------------------ 方案二：奶油盐系 核心色值 ------------------
val CreamBackground = Color(0xFFFFFDF9)   // 奶油底色
val SageGreen = Color(0xFFB8C4BB)        // 鼠尾草绿 (主色：运行状态/按钮)
val MilkTeaBrown = Color(0xFFD4A373)      // 奶茶棕 (辅助色：图标/分类)
val LemonYellow = Color(0xFFFDFFB6)       // 柠檬黄 (强调色：高亮/重要提醒)
val WarmCharcoal = Color(0xFF4A4A4A)      // 暖炭灰 (文字：比纯黑更柔和)
val PaperWhite = Color(0xFFFFFFFF)        // 卡片色：纯白，用于在奶油背景上产生层次感

// ------------------ 方案二 Light Color Scheme ------------------
private val CreamSalt = lightColorScheme(
    primary = SageGreen,
    onPrimary = Color.White,
    secondary = MilkTeaBrown,
    onSecondary = Color.White,
    tertiary = LemonYellow,
    onTertiary = WarmCharcoal,
    background = CreamBackground,
    surface = PaperWhite,
    onBackground = WarmCharcoal,
    onSurface = WarmCharcoal,
    outline = SageGreen.copy(alpha = 0.3f) // 用于卡片边框，增加精致感
)


// --- 排版建议：使用简洁的无衬线字体 ---
val AppTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 0.5.sp,
        color = DeepRockGray
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = DeepRockGray
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = DeepRockGray.copy(alpha = 0.6f)
    )
)

@Composable
fun AutoScriptAppTheme(
    themeStyle: AppThemeStyle = AppThemeStyle.MINT_SODA, // 默认主题
    content: @Composable () -> Unit
) {
    val targetColorScheme = when (themeStyle) {
        AppThemeStyle.MINT_SODA -> MintSoda // 方案一的 ColorScheme
        AppThemeStyle.CREAM_SALT -> CreamSalt // 方案二的 ColorScheme
    }

    MaterialTheme(
        colorScheme = targetColorScheme,
        typography = AppTypography,
        content = content
    )
}
