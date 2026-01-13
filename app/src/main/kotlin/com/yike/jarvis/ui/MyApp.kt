package com.yike.jarvis.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yike.jarvis.feature.beverage.ui.BeverageTracker
import com.yike.jarvis.feature.home.ui.Home
import com.yike.jarvis.feature.netease.ui.Netease
import com.yike.jarvis.feature.scheduler.ui.TaskScheduler
import com.yike.jarvis.feature.scheduler.ui.TaskSchedulerViewModel
import com.yike.jarvis.feature.taobao.ui.Taobao
import com.yike.jarvis.ui.navigation.Routes
import com.yike.jarvis.ui.settings.SettingsScreen
import com.yike.jarvis.ui.theme.AutoScriptAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MyApp() {
    val viewModel: TaskSchedulerViewModel = hiltViewModel()

    AutoScriptAppTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                NavHost(navController = navController, startDestination = Routes.HOME) {
                    composable(Routes.HOME) { Home(navController) }
                    composable(Routes.TAOBAO) { Taobao() }
                    composable(Routes.NETEASE) { Netease(navController) }
                    composable(Routes.BEVERAGE_TRACKER) { BeverageTracker() }
                    composable(Routes.TASK_SCHEDULER) { TaskScheduler(viewModel) }
                    composable(Routes.SETTINGS) { 
                        SettingsScreen(onBack = { navController.popBackStack() }) 
                    }
                }
            }
        }
    }
}