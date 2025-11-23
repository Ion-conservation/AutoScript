package auto.script

import android.app.Application
import android.util.Log
import auto.script.common.ScriptCountdownManager
import auto.script.shizuku.ShizukuManager
import auto.script.viewmodel.AutomationViewModel
import rikka.shizuku.Shizuku


class MyApp : Application() {

    lateinit var shizukuManager: ShizukuManager

    val automationViewModel: AutomationViewModel by lazy { AutomationViewModel() }

    companion object {
        private const val TAG = "AutoScriptApp"
        lateinit var instance: MyApp
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        // 正确：监听 binder 就绪
        Shizuku.addBinderReceivedListener {
            Log.d(TAG, "Shizuku binder received")
            shizukuManager.init(this) // ← 现在安全初始化
        }


        // 启动前台倒计时检查（仅在进程存活时）
        ScriptCountdownManager.startCountdownChecker(this)

        // 启动后台 WorkManager 检查（即使 APP 没启动也能运行）
        ScriptCountdownManager.startBackgroundCountdown(this)

        // 设置每天中午 12 点提醒
//        ScriptCountdownManager.scheduleDailyReminder(this)

        ScriptCountdownManager.scheduleTestInOneMinute(this)

    }


}
