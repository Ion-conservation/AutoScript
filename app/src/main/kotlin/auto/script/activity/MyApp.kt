package auto.script.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import auto.script.feature.beverage.BeverageTracker
import auto.script.feature.netease.Netease
import auto.script.feature.scheduler.TaskScheduler
import auto.script.ui.theme.AutoScriptAppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MyApp() {
    AutoScriptAppTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Auto Script",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    navigationIcon = {
                        if (currentRoute != Routes.HOME) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { }) {
                    Text("-")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(navController = navController, startDestination = Routes.HOME) {
                    composable(Routes.HOME) { Home(navController) }
                    composable(Routes.NETEASE) { Netease(navController) }
                    composable(Routes.TAOBAO) { Taobao() }
                    composable(Routes.AUTOMATION_HUB) { Netease(navController) }
                    composable(Routes.BEVERAGE_TRACKER) { BeverageTracker() }
                    composable(Routes.TASK_SCHEDULER) { TaskScheduler() }
                }
            }
        }
    }
}
