package com.yike.jarvis.feature.netease.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.jarvis.core.shizuku.service.ShizukuBindState
import com.yike.jarvis.core.shizuku.service.ShizukuGrantState
import com.yike.jarvis.core.shizuku.service.ShizukuRunningState
import com.yike.jarvis.core.shizuku.service.ShizukuStatus
import com.yike.jarvis.ui.theme.DashboardColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeteaseScreen(
    isA11yServiceReady: Boolean,
    shizukuStatus: ShizukuStatus,
    consoleOutput: List<String>,
    openA11ySettings: () -> Unit,
    initShizukuTool: () -> Unit,
    onStart: () -> Unit,
) {

    Scaffold(
        topBar = {
            Column() {
                TopAppBar(
                    title = {
                        Text(
                            text = "⚡ 网易云音乐自动化",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )

                HorizontalDivider(
                    thickness = 0.5.dp, // 线条粗细，建议 0.5 到 1 dp
                    color = MaterialTheme.colorScheme.outlineVariant // 使用主题中的虚线条颜色
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 主卡片
            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, DashboardColors.PrimaryVariant)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {


                    // 状态指示器
                    ServiceStatusRow(
                        "• Accessibility",
                        isA11yServiceReady,
                        DashboardColors.Accent
                    ) { openA11ySettings() }

                    ServiceStatusRow(
                        "• Shizuku",
                        shizukuStatus.bind == ShizukuBindState.BINDED,
                        DashboardColors.Accent
                    ) { initShizukuTool() }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 状态提示文本
                    StatusText(isA11yServiceReady, shizukuStatus)

                    Spacer(modifier = Modifier.height(12.dp))

                    // 控制按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onStart,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DashboardColors.Primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = isA11yServiceReady && shizukuStatus.bind == ShizukuBindState.BINDED
                        ) {
                            Text(
                                "▶ 启动脚本",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { /* TODO: 停止功能 */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DashboardColors.PrimaryVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "⊟ 停止脚本",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Console 输出
                    ConsoleOutput(
                        logs = consoleOutput,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceStatusRow(
    label: String,
    isReady: Boolean,
    accentColor: Color,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (isReady) accentColor else DashboardColors.TextSecondary
        )
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isReady) accentColor else DashboardColors.TextSecondary
            ),
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                text = if (isReady) "✓" else "Settings",
                fontSize = 12.sp,
                color = if (isReady) Color.Black else Color.White
            )
        }
    }
}

@Composable
fun StatusText(
    isA11yServiceReady: Boolean,
    shizukuStatus: ShizukuStatus
) {
    val statusText = when {
        !isA11yServiceReady -> "⚠ 请先启用无障碍服务"
        shizukuStatus.running == ShizukuRunningState.NOT_RUNNING -> "⚠ Shizuku 未运行"
        shizukuStatus.grant == ShizukuGrantState.NOT_GRANTED -> "⚠ Shizuku 未授权"
        shizukuStatus.bind == ShizukuBindState.NOT_BINDED -> "⚠ Shizuku 未绑定服务"
        else -> "✓ 所有服务已就绪，可以开始"
    }
    val statusColor = when {
        !isA11yServiceReady -> DashboardColors.SecondaryVariant
        shizukuStatus.bind != ShizukuBindState.BINDED -> DashboardColors.SecondaryVariant
        else -> DashboardColors.Accent
    }

    Text(
        text = statusText,
        fontSize = 14.sp,
        color = statusColor,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun ConsoleOutput(
    logs: List<String>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF0A0A0A),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Console output",
                fontSize = 12.sp,
                color = DashboardColors.TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (logs.isEmpty()) {
                Text(
                    text = "$ Waiting for command...",
                    fontSize = 11.sp,
                    color = DashboardColors.TextSecondary
                )
            } else {
                logs.forEach { log ->
                    Text(
                        text = log,
                        fontSize = 11.sp,
                        color = when {
                            log.contains("✓") || log.contains("▶") -> DashboardColors.Accent
                            log.contains("✖") || log.contains("⊟") -> DashboardColors.Primary
                            log.contains("⚠") -> DashboardColors.SecondaryVariant
                            log.contains("➤") || log.contains("•") -> DashboardColors.Secondary
                            else -> DashboardColors.TextSecondary
                        },
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }

        // 自动滚动到最后
        LaunchedEffect(logs.size) {
            if (logs.isNotEmpty()) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }
}
