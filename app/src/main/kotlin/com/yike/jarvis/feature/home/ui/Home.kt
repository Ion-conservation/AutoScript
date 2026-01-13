package com.yike.jarvis.feature.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yike.jarvis.R
import com.yike.jarvis.ui.navigation.Routes
import com.yike.jarvis.ui.theme.DashboardColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Scaffold(
        topBar = {
            Column() {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.jarvis_launcher_playstore),
                                contentDescription = stringResource(id = R.string.app_icon_description),
                                modifier = Modifier
                                    .size(32.dp) // 设置图标大小为 32dp
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Text(
                                "Jarvis",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                        }

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

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            HomeCard("🎵 网易云脚本", DashboardColors.Primary) {
                navController.navigate(Routes.NETEASE)
            }

            HomeCard("🛒 淘宝脚本", DashboardColors.Secondary) {
                navController.navigate(Routes.TAOBAO)
            }

            HomeCard("🥤 饮料追踪", DashboardColors.SecondaryVariant) {
                navController.navigate(Routes.BEVERAGE_TRACKER)
            }

            HomeCard("⏰ Task Scheduler", DashboardColors.Accent) {
                navController.navigate(Routes.TASK_SCHEDULER)
            }

            HomeCard("⚙️ 设置 (备份/恢复)", DashboardColors.Background) {
                navController.navigate(Routes.SETTINGS)
            }
        }
    }
}

@Composable
fun HomeCard(text: String, accentColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(2.dp, accentColor)
    ) {
        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Companion.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Companion.Bold,
                color = accentColor,
                textAlign = TextAlign.Companion.Center
            )
        }
    }
}