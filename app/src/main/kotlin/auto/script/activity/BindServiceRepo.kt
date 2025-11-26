package auto.script.activity

import auto.script.common.BindServiceStatus
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
class BindServiceRepo @Inject constructor() {
    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    fun updateConnectStatus(isConnected: Boolean) {
        _isConnected.value = isConnected
    }

    val bindServiceStatus: StateFlow<BindServiceStatus> = isConnected.map { connected ->
        if (connected) BindServiceStatus.READY else BindServiceStatus.NOT_CONNECTED
    }.stateIn(
        CoroutineScope(Dispatchers.Default),
        SharingStarted.Eagerly,
        BindServiceStatus.NOT_CONNECTED
    )
}