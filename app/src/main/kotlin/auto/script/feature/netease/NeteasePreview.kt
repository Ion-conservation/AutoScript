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
        openA11ySettings = {},
        initShizukuTool = {},
        onStart = {}
    )
}
