package auto.script.shizuku

import android.util.Log
import auto.script.common.ShizukuStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShizukuRepo @Inject constructor() {

    // ------------------ Shizuku 连接状态 ------------------
    private val _shizukuIsConnected = MutableStateFlow(false)
    val shizukuIsConnected: StateFlow<Boolean> = _shizukuIsConnected

    fun updateConnectStatus(connectStatus: Boolean) {
        Log.i("ShizukuRepo", "updateConnectStatus")
        _shizukuIsConnected.value = connectStatus
    }

    // ------------------ Shizuku 授权状态 ------------------
    private val _shizukuIsGranted = MutableStateFlow(false)
    val shizukuIsGranted: StateFlow<Boolean> = _shizukuIsGranted

    fun updateGrantedStatus(grantedStatus: Boolean) {
        Log.i("ShizukuRepo", "updateGrantedStatus")
        _shizukuIsGranted.value = grantedStatus
    }

    // ------------------ Shizuku 状态 ------------------
    val shizukuStatus =
        combine(shizukuIsConnected, shizukuIsGranted) { shizukuIsConnected, shizukuIsGranted ->
            when {
                !shizukuIsConnected -> ShizukuStatus.NOT_CONNECTED
                shizukuIsConnected && !shizukuIsGranted -> ShizukuStatus.NOT_GRANTED
                else -> ShizukuStatus.GRANTED
            }
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
            started = SharingStarted.Eagerly,
            initialValue = ShizukuStatus.NOT_CONNECTED
        )
}