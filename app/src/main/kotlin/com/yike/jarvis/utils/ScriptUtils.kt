package com.yike.jarvis.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings

object ScriptUtils {

    const val TAG = "ScriptUtils"

    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }


    /**
     * 引导用户到系统的辅助功能设置页面
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }


    /**
     * 启动普通 APP
     * 通过 PackageManager 找到 APP 的 Activity 入口并启动。
     * */
    fun openApp(packageName: String) {
        val pm = appContext.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)

        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            appContext.startActivity(it)
            ScriptLogger.i(TAG, "正在打开 $packageName App...")
        }
    }

    /**
     * 启动无障碍设置
     * ActivityContext 可以直接启动 Activity
     * 在 Application / Service / BroadcastReceiver 里调用 必须加 FLAG_ACTIVITY_NEW_TASK，否则会崩溃。
     * */
    fun openA11yServiceSetting() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }




}


