package auto.script

import android.app.Application
import android.util.Log
import auto.script.broadcast.BroadcastManager
import auto.script.broadcast.ScriptBroadcast

import auto.script.shizuku.ShizukuManager

import rikka.shizuku.Shizuku

class MyApp : Application() {

    companion object {
        private const val TAG = "AutoScriptApp"
        lateinit var instance: MyApp
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        initBroadcastManager()

        checkAccessibilityService()

        // 正确：监听 binder 就绪
        Shizuku.addBinderReceivedListener {
            Log.d(TAG, "Shizuku binder received")
            ShizukuManager.init(this) // ← 现在安全初始化
        }

    }

    private fun initBroadcastManager() {
        val broadcastManager = BroadcastManager(this)
        ScriptBroadcast.init(broadcastManager)
    }

    private fun checkAccessibilityService() {

    }
}
