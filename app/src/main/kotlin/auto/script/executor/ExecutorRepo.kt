package auto.script.executor

import auto.script.service.AutomationService
import auto.script.shizuku.ShizukuManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExecutorRepo @Inject constructor() {

    @Inject
    lateinit var shizukuManager: ShizukuManager

    @Inject
    lateinit var automationService: AutomationService

    val taobaoExecutor: TaobaoExecutor by lazy {
        // 1. 获取 A11yCapability 实例
        val a11yCapabilityInstance = automationService
            ?: throw IllegalStateException("无障碍服务实例不存在，请检查代码！")

        val shizukuService = shizukuManager?.getService()
            ?: throw IllegalStateException("UserService 实例不存在，请检查代码！")

        // 2. 实例化 TaobaoExecutor，注入两个依赖
        TaobaoExecutor(a11yCapabilityInstance, shizukuService)

    }

    val cloudmusicExecutor: CloudmusicExecutor by lazy {
        // 1. 获取 A11yCapability 实例
        val a11yCapabilityInstance = automationService
            ?: throw IllegalStateException("无障碍服务实例不存在，请检查代码！")
        val shizukuService = shizukuManager?.getService()
            ?: throw IllegalStateException("UserService 实例不存在，请检查代码！")

        CloudmusicExecutor(a11yCapabilityInstance, shizukuService)
    }

//    fun getCloudmusicExecutor(): CloudmusicExecutor? {
//        return cloudmusicExecutor
//    }
//
//    fun getTaobaoExecutor(): TaobaoExecutor? {
//        return taobaoExecutor
//    }

}