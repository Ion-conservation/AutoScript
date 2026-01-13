package com.yike.jarvis.feature.taobao.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Taobao() {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(100.dp),
    ) {
        Text(
            text = "淘宝脚本",
            modifier = Modifier.Companion
                .padding(16.dp),
            textAlign = TextAlign.Companion.Center,
        )
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                16.dp,
                Alignment.Companion.CenterHorizontally
            )
        ) {
            Button(
                onClick = { },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("启动")
            }
            Button(
                onClick = { },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text("停止")
            }
        }
    }
}