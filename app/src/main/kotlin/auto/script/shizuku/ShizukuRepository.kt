package auto.script.shizuku

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShizukuRepository @Inject constructor() {

    // ------------------ Shizuku 使用流程 ------------------
    /**
     * Shizuku 使用流程
     * 1. 检查 Shizuku APP 运行状态
     * 2. 检查 APP 授权
     * 3. bindUserService 绑定 UserService
     * 4. 调用 ShizukuTool 中的方法
     * */

    // ------------------ Shizuku 状态 ------------------
    private val _shizukuStatus = MutableStateFlow(
        ShizukuStatus(
            running = ShizukuRunningState.NOT_RUNNING,
            grant = ShizukuGrantState.NOT_GRANTED,
            bind = ShizukuBindState.NOT_BINDED
        )
    )
    val shizukuStatus = _shizukuStatus.asStateFlow()


    fun updateShizukuStatus(status: ShizukuStatus) {
        Log.i("Shizukurepo", "updateShizukuStatus")
        _shizukuStatus.value = status
    }

    // ------------------ Shizuku UserService 实例 ------------------
    private var _userService: IUserService? = null
    fun attachUserServiceInstance(userService: IUserService?) {
        _userService = userService
    }

    fun getMyShizukuService(): IUserService? {
        return _userService
    }


    fun <T> withService(block: (IUserService) -> T): T {
        val service = _userService
            ?: throw IllegalStateException("Shizuku 未连接或 UserService 未绑定")

        return block(service)
    }

}