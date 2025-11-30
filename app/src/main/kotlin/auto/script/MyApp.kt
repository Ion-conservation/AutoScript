package auto.script

import android.app.Application
import auto.script.common.ScriptCountdownManager
import auto.script.utils.ScriptLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {

    companion object {
        private const val TAG = "Auto.Script.App"
        lateinit var instance: MyApp
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        ScriptLogger.init(this)
        ScriptLogger.i(TAG, "自动化服务已启动")


        // 启动前台倒计时检查（仅在进程存活时）
        ScriptCountdownManager.startCountdownChecker(this)

        // 启动后台 WorkManager 检查（即使 APP 没启动也能运行）
        ScriptCountdownManager.startBackgroundCountdown(this)

        // 设置每天中午 12 点提醒
//        ScriptCountdownManager.scheduleDailyReminder(this)

        ScriptCountdownManager.scheduleTestInOneMinute(this)

    }


}
