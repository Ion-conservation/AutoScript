package com.yike.jarvis.feature.netease.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.yike.jarvis.core.shizuku.service.ShizukuBindState
import com.yike.jarvis.core.shizuku.service.ShizukuGrantState
import com.yike.jarvis.core.shizuku.service.ShizukuRunningState
import com.yike.jarvis.core.shizuku.service.ShizukuStatus

@Preview(showBackground = true)
@Composable
fun PreviewNeteaseScreen() {
    NeteaseScreen(
        isA11yServiceReady = true,
        shizukuStatus = ShizukuStatus(
            running = ShizukuRunningState.RUNNING,
            grant = ShizukuGrantState.GRANTED,
            bind = ShizukuBindState.BINDED
        ),
        consoleOutput = listOf(
            "[12:30:45] ▶ 启动脚本...",
            "[12:30:46] ✓ 打开应用成功",
            "[12:30:47] ➤ 正在启动应用，检测启动广告...",
            "[12:30:48] • 无启动广告，继续"
        ),
        openA11ySettings = {},
        initShizukuTool = {},
        onStart = {}
    )
}
