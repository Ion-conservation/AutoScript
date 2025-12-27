package auto.script.feature.netease

import android.util.Log
import auto.script.A11yService.A11yServiceRepository
import auto.script.shizuku.ShizukuBindState
import auto.script.shizuku.ShizukuManager
import auto.script.shizuku.ShizukuRepository
import auto.script.utils.ScriptUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NeteaseController @Inject constructor(
    private val executor: NeteaseExecutor,
    private val a11yServiceRepository: A11yServiceRepository,
    private val shizukuRepository: ShizukuRepository,
    private val shizukuManager: ShizukuManager,
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
        shizukuManager.initShizukuTool()
    }

}