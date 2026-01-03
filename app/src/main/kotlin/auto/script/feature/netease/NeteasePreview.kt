package auto.script.feature.netease

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import auto.script.shizuku.ShizukuBindState
import auto.script.shizuku.ShizukuGrantState
import auto.script.shizuku.ShizukuRunningState
import auto.script.shizuku.ShizukuStatus

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
