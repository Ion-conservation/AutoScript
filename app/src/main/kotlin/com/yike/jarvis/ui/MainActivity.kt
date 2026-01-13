package com.yike.jarvis.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yike.jarvis.activity.LogListActivity
import com.yike.jarvis.feature.scheduler.ui.TaskSchedulerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private val taskSchedulerViewModel: TaskSchedulerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化任务调度通知功能
        taskSchedulerViewModel.applicationContext = this
        taskSchedulerViewModel.startTaskMonitoring()

        setContent {
            MyApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止任务监视器
        taskSchedulerViewModel.stopTaskMonitoring()
    }


    fun shareLog() {
//        LogSharer.shareLogToWeChat(this)

        val intent = Intent(this, LogListActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Service 启动 Activity 必须加
        startActivity(intent)
    }

//    fun openLogManager() {
//        val intent = Intent(this, LogListActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Service 启动 Activity 必须加
//        startActivity(intent)
//    }


}