package auto.script.service

import auto.script.common.A11yServiceStatus
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
class AutomationServiceRepo @Inject constructor() {

    // ------------------ A11yService 连接状态 ------------------
    private val _isA11yServiceConnected = MutableStateFlow(false)
    val isA11yServiceConnected = _isA11yServiceConnected.asStateFlow()

    fun updateA11yServiceConnected(connected: Boolean) {
        _isA11yServiceConnected.value = connected
    }

    // ------------------ A11yService 状态 ------------------
    val a11yServiceState: StateFlow<A11yServiceStatus> = isA11yServiceConnected.map { connected ->
        if (connected) A11yServiceStatus.READY else A11yServiceStatus.NOT_GRANTED
    }.stateIn(
        CoroutineScope(Dispatchers.Default),
        SharingStarted.Eagerly,
        A11yServiceStatus.NOT_GRANTED
    )

}