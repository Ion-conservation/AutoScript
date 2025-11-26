package auto.script.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import auto.script.shizuku.ShizukuManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
@Singleton
class AutomationBridgeService : Service() {

    @Inject
    lateinit var automationBridgeServiceRepo: AutomationBridgeServiceRepo

    @Inject
    lateinit var shizukuManager: ShizukuManager
    private val automationService get() = automationBridgeServiceRepo.automationServiceRepo.getService()


    // **使用延迟初始化 (lazy)，确保在需要时才创建 TaobaoExecutor**


    inner class LocalBinder : Binder() {
        // **通过 Binder 暴露 TaobaoExecutor 实例**
//        fun getTaobaoExecutor(): TaobaoExecutor = taobaoExecutor
//        fun getCloudmusicExecutor(): CloudmusicExecutor = cloudmusicExecutor
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("AutomationBridgeService", "onBind")
        automationBridgeServiceRepo.updateBridgeServiceConnected(true)
        return LocalBinder()
    }


    override fun onUnbind(intent: Intent?): Boolean {
        automationBridgeServiceRepo.updateBridgeServiceConnected(false)
        return super.onUnbind(intent)
    }


}