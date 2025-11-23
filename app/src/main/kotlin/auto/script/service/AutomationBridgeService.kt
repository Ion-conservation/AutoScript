package auto.script.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import auto.script.executor.CloudmusicExecutor
import auto.script.executor.TaobaoExecutor
import auto.script.shizuku.ShizukuManager


class AutomationBridgeService : Service() {
    lateinit var shizukuManager: ShizukuManager
    lateinit var automationService: AutomationService


    // **使用延迟初始化 (lazy)，确保在需要时才创建 TaobaoExecutor**
    private val taobaoExecutor: TaobaoExecutor by lazy {
        // 1. 获取 A11yCapability 实例
        val a11yCapabilityInstance = automationService
            ?: throw IllegalStateException("无障碍服务实例不存在，请检查代码！")

        val shizukuService = shizukuManager.getService()
            ?: throw IllegalStateException("UserService 实例不存在，请检查代码！")

        // 2. 实例化 TaobaoExecutor，注入两个依赖
        val executor = TaobaoExecutor(a11yCapabilityInstance, shizukuService)

        // 【核心修改点 1】：赋值给 AutomationService 的静态变量
        automationService.taobaoExecutor = executor

        executor
    }

    private val cloudmusicExecutor: CloudmusicExecutor by lazy {
        // 1. 获取 A11yCapability 实例
        val a11yCapabilityInstance = automationService
            ?: throw IllegalStateException("无障碍服务实例不存在，请检查代码！")
        val shizukuService = shizukuManager.getService()
            ?: throw IllegalStateException("UserService 实例不存在，请检查代码！")

        val executor = CloudmusicExecutor(a11yCapabilityInstance, shizukuService)

        // 【核心修改点 2】：赋值给 AutomationService 的静态变量
        automationService.cloudmusicExecutor = executor

        executor
    }

    inner class LocalBinder : Binder() {
        // **通过 Binder 暴露 TaobaoExecutor 实例**
        fun getTaobaoExecutor(): TaobaoExecutor = taobaoExecutor
        fun getCloudmusicExecutor(): CloudmusicExecutor = cloudmusicExecutor
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("AutomationBridgeService", "onBind")
        return if (automationService == null) null else LocalBinder()
    }
}