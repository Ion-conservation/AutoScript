package auto.script.feature.netease

import androidx.lifecycle.ViewModel
import auto.script.A11yService.A11yServiceRepository
import auto.script.shizuku.ShizukuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NeteaseViewModel @Inject constructor(
    private val controller: NeteaseController,
    private val a11yServiceRepository: A11yServiceRepository,
    private val shizukuRepository: ShizukuRepository
) : ViewModel() {

    val onA11yConnected = a11yServiceRepository.onConnected
    val isA11yServiceReady = controller.isA11yServiceReady
    val shizukuStatus = controller.shizukuStatus

    fun startAutomation() = controller.startAutomation()
    fun openA11ySettings() = controller.openAccessibilitySettings()
    fun initShizukuTool() = controller.initShizukuTool()

}
