package auto.script.feature.netease.controller

import android.util.Log
import auto.script.core.a11y.repository.A11yServiceRepository
import auto.script.core.shizuku.repository.ShizukuRepository
import auto.script.core.shizuku.service.ShizukuBindState
import auto.script.core.shizuku.service.ShizukuService
import auto.script.feature.netease.executor.NeteaseExecutor
import auto.script.utils.ScriptUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NeteaseController @Inject constructor(
    private val executor: NeteaseExecutor,
    private val a11yServiceRepository: A11yServiceRepository,
    private val shizukuRepository: ShizukuRepository,
    private val shizukuService: ShizukuService,
) {

    fun startAutomation() {
        if (!a11yServiceRepository.isReady.value) {
            Log.i("NeteaseController", "A11yService 未准备好")
            return
        }
        if (!(shizukuRepository.shizukuStatus.value.bind == ShizukuBindState.BINDED)) {
            Log.i("NeteaseController", "Shizuku 未准备好")
            return
        }
        executor.startAutomation()
    }

    fun openAccessibilitySettings() {
        ScriptUtils.openA11yServiceSetting()
    }

    val isA11yServiceReady = a11yServiceRepository.isReady
    val shizukuStatus = shizukuRepository.shizukuStatus


    fun initShizukuTool() {
        shizukuService.initShizukuTool()
    }

}