package auto.script

import android.app.Application
import android.util.Log
import auto.script.utils.ScriptUtils

class MyApp : Application() {

    companion object {
        private const val TAG = "AutoScriptApp"
    }

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "AutoScriptApp onCreate")

        // 检查服务是否启用
        val taobaoEnabled =
            ScriptUtils.isAccessibilityServiceEnabled(applicationContext, TaobaoService::class.java)
        val cloudmusicEnabled =
            ScriptUtils.isAccessibilityServiceEnabled(
                applicationContext,
                CloudMusicService::class.java
            )

        Log.d(TAG, "TaobaoService enabled: $taobaoEnabled")
        Log.d(TAG, "CloudmusicService enabled: $cloudmusicEnabled")

        // 你可以在这里做更多初始化，比如设置全局变量、初始化日志系统等
    }
}
