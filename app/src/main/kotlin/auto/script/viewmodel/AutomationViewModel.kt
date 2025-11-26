package auto.script.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auto.script.activity.BindServiceRepo
import auto.script.common.A11yServiceStatus
import auto.script.common.BindServiceStatus
import auto.script.common.ShizukuStatus
import auto.script.service.AutomationServiceRepo
import auto.script.shizuku.ShizukuRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


data class ButtonUiState(
    val text: String,
    val buttonEnabled: Boolean
)


@HiltViewModel
class AutomationViewModel @Inject constructor(
    private val shizukuRepository: ShizukuRepo,
    private val automationServiceRepo: AutomationServiceRepo,
//    private val automationBridgeServiceRepo: AutomationBridgeServiceRepo,
    private val bindServiceRepo: BindServiceRepo
) : ViewModel() {

    val a11yServiceUiState = automationServiceRepo.a11yServiceState.map { status ->
        when (status) {
            A11yServiceStatus.NOT_GRANTED -> ButtonUiState("A11yService 未授权", true)
            A11yServiceStatus.READY -> ButtonUiState("A11yService 已授权", false)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ButtonUiState("A11yService 检测中...", true)
    )

    val shizukuUiState = shizukuRepository.shizukuStatus.map { status ->
        when (status) {
            ShizukuStatus.NOT_CONNECTED -> ButtonUiState("Shizuku 未连接", true)
            ShizukuStatus.NOT_GRANTED -> ButtonUiState("Shizuku 未授权", true)
            ShizukuStatus.GRANTED -> ButtonUiState("Shizuku 已授权", false)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ButtonUiState("Shizuku 检测中...", true))


//    val bridgeServiceUiState = automationBridgeServiceRepo.bridgeServiceState.map { status ->
//        when (status) {
//            BridgeServiceStatus.NOT_BIND -> ButtonUiState("Bridge Service 未连接", true)
//            BridgeServiceStatus.READY -> ButtonUiState("Bridge Service 已连接", false)
//        }
//    }.stateIn(
//        viewModelScope,
//        SharingStarted.Eagerly,
//        ButtonUiState("Bridge Service 检测中...", true)
//    )


    val bindServiceUiState = bindServiceRepo.bindServiceStatus.map { status ->
        when (status) {
            BindServiceStatus.NOT_CONNECTED -> ButtonUiState("Bind Service 未连接", true)
            BindServiceStatus.READY -> ButtonUiState("Bind Service 已连接", false)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ButtonUiState("Bind Service 检测中...", true)
    )
}
