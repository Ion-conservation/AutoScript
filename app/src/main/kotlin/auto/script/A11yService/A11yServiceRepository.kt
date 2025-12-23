package auto.script.A11yService

import android.accessibilityservice.AccessibilityService
import android.util.Log
import auto.script.common.A11yServiceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class A11yServiceRepository @Inject constructor() {

    // ------------------ A11yService 连接状态 ------------------
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    fun updateA11yServiceConnectState(connected: Boolean) {
        _isReady.value = connected
    }

    // ------------------ A11yService 实例 ------------------
    private var _A11yServiceInstance: AccessibilityService? = null

    fun attachServiceInstance(service: AccessibilityService) {
        _A11yServiceInstance = service
    }

    fun <T> withService(block: (AccessibilityService) -> T): T? {
        val service = _A11yServiceInstance
            ?: throw IllegalStateException("A11yService 未连接")

        return block(service)
    }


    // ------------------ 返回事件 ------------------
    private val _onConnected = MutableSharedFlow<Unit>(replay = 1)
    val onConnected = _onConnected.asSharedFlow()

    fun notifyConnected() {
        Log.i("A11yService", "onServiceConnected triggered")

        _onConnected.tryEmit(Unit)
    }


    // ------------------ A11yService State ------------------
    val a11yServiceState: StateFlow<A11yServiceStatus> = isReady.map { connected ->
        if (connected) A11yServiceStatus.READY else A11yServiceStatus.NOT_GRANTED
    }.stateIn(
        CoroutineScope(Dispatchers.Default),
        SharingStarted.Eagerly,
        A11yServiceStatus.NOT_GRANTED
    )

}