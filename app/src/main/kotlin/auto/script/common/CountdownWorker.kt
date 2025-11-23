package auto.script.common

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class CountdownWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val targetTime = ScriptCountdownManager.targetTimeMillis
        if (targetTime != null) {
            val diff = targetTime - System.currentTimeMillis()
            if (diff <= 0) {
                ScriptCountdownManager.showNotification(
                    applicationContext,
                    "脚本提醒",
                    "时间到了，立即执行脚本！"
                )
                ScriptCountdownManager.targetTimeMillis = null
            }
        }
        return Result.success()
    }
}
