package auto.script.service

import auto.script.common.BridgeServiceStatus
import auto.script.shizuku.ShizukuRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutomationBridgeServiceRepo @Inject constructor(
    val shizukuRepo: ShizukuRepo,
    val automationServiceRepo: AutomationServiceRepo
) {
    // --- 连接状态 ---
    private val _isBridgeServiceConnected = MutableStateFlow(false)
    val isBridgeServiceConnected = _isBridgeServiceConnected.asStateFlow()

    fun updateBridgeServiceConnected(connected: Boolean) {
        _isBridgeServiceConnected.value = connected
    }

    val bridgeServiceState: StateFlow<BridgeServiceStatus> =
        isBridgeServiceConnected.map { connected ->
            if (connected) BridgeServiceStatus.READY else BridgeServiceStatus.NOT_BIND
        }.stateIn(
            CoroutineScope(Dispatchers.Default),
            SharingStarted.Eagerly,
            BridgeServiceStatus.NOT_BIND
        )
}