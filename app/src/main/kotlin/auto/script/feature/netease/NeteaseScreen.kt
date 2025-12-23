package auto.script.feature.netease

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import auto.script.shizuku.ShizukuBindState
import auto.script.shizuku.ShizukuGrantState
import auto.script.shizuku.ShizukuRunningState
import auto.script.shizuku.ShizukuStatus


@Composable
fun NeteaseScreen(
    isA11yServiceReady: Boolean,
    shizukuStatus: ShizukuStatus,
    openA11ySettings: () -> Unit,
    initShizukuTool: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // ------------------ A11y 状态卡片 ------------------
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isA11yServiceReady) "A11yService 准备好了" else "A11yService 还没准备好",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = openA11ySettings,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("打开无障碍设置")
                }
            }
        }

        // ------------------ Shizuku 状态卡片 ------------------
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when {
                        shizukuStatus.running == ShizukuRunningState.NOT_RUNNING -> "Shizuku 未运行"
                        shizukuStatus.grant == ShizukuGrantState.NOT_GRANTED -> "Shizuku 未授权"
                        shizukuStatus.bind == ShizukuBindState.NOT_BINDED -> "Shizuku 未绑定服务"
                        else -> "Shizuku 已就绪"
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )


                Button(
                    onClick = initShizukuTool,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        text = when {
                            shizukuStatus.running == ShizukuRunningState.NOT_RUNNING -> "启动 Shizuku APP"
                            shizukuStatus.grant == ShizukuGrantState.NOT_GRANTED -> "到 Shizuku 授权"
                            shizukuStatus.bind == ShizukuBindState.NOT_BINDED -> "绑定 Shizuku 服务"
                            else -> "Shizuku 已就绪"
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 网易云脚本卡片
        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color(0xFFE0F7FA)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "网易云脚本",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    Button(
                        onClick = onStart,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("启动")
                    }
                    Button(
                        onClick = onStop,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("停止")
                    }
                }
            }
        }
    }
}
