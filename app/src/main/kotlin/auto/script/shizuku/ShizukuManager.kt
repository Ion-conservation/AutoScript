package auto.script.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import auto.script.BuildConfig
import rikka.shizuku.Shizuku

/**
 * Shizuku APP 最重要的功能是作为中介，接收应用请求，发送到系统服务器，并返回结果，相当于服务端。
 * Shizuku 库 的作用是在应用中，发指令给 Shizuku APP，相当于客户端。
 * 使用流程：1. 检查 Shizuku APP 授权
 * 2. 绑定 UserService
 * 3. 返回 iAssistService 实例，然后使用实例调用 UserService 中实现的功能，例如 instance.openApp
 * */


class ShizukuManager {
    private val TAG = "ShizukuManager"
    private val REQUEST_CODE = 100
    private val SHIZUKU_PERMISSION_REQUEST_CODE = 1001

    // UserService 实例，暴露出去其他模块使用

    var iAssistService: IAssistService? = null
    private var isBound = false

    private val handler = Handler(Looper.getMainLooper())

    var onStatusChanged: ((Boolean) -> Unit)? = null

    // ------------------ Shizuku 生命周期 ------------------
    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name)
    )
        .daemon(false)
        .processNameSuffix("assist")
        .debuggable(BuildConfig.DEBUG)

    // 请求 Shizuku 权限的回调函数，授权 | 被拒绝
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { req, grant ->
        if (req == REQUEST_CODE && grant == PackageManager.PERMISSION_GRANTED) {
            bindUserService()
        } else {
            Log.w(TAG, "用户已拒绝 Shizuku 权限。")
        }
    }

    // Shizuku 连接成功的回调 和 断开连接的回调
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Shizuku 服务连接成功")
            iAssistService = IAssistService.Stub.asInterface(service)
            isBound = true

            onStatusChanged?.invoke(true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "Shizuku 服务断开连接，2 秒后尝试重连。")
            iAssistService = null
            isBound = false
            onStatusChanged?.invoke(false)
            // 自动重连
            handler.postDelayed({ bindUserService() }, 2000)
        }
    }

    // ------------------ Shizuku 核心方法 ------------------

    fun init(context: Context) {
        Log.d(TAG, "ShizukuManager 初始化")

        // 绑定授权结果的回调函数
        Shizuku.addRequestPermissionResultListener(permissionListener)

        var checkPermissionResult: Boolean = checkShizukuPermission()

        if (checkPermissionResult) {
            Log.i(TAG, "Shizuku 已授权应用 ADB 权限，开始绑定 UserService 服务。")
            bindUserService()
        } else {
            Log.i(TAG, "Shizuku 未授权应用 ADB 权限，请授权。")
            Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
        }
    }

    fun isShizukuRunning(): Boolean {
        // 检查 Shizuku 服务是否已启动
        return Shizuku.pingBinder()
    }

    fun requestPermission() {
        Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
    }

    fun checkShizukuPermission(): Boolean {
        if (Shizuku.isPreV11()) {
            Log.e(TAG, "Shizuku 版本太旧，请升级。")
            return false
        }
        return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }

    private fun bindUserService() {
        try {
            Log.d(TAG, "正在绑定 UserService...")
            Shizuku.bindUserService(userServiceArgs, serviceConnection)
        } catch (e: Exception) {
            Log.e(TAG, "绑定失败，3 秒后重试", e)
            handler.postDelayed({ bindUserService() }, 3000)
        }
    }


    // 提供安全调用封装
    inline fun <T> withService(block: IAssistService.() -> T): T? {
        return if (iAssistService == null) {
            Log.w("ShizukuManager", "iAssistService is NULL, block 未执行")
            null
        } else {
            Log.d("ShizukuManager", "iAssistService 已就绪，执行 block")
            iAssistService!!.block()
        }
    }

    fun getService(): IAssistService? = iAssistService
    fun isConnected(): Boolean = isBound && iAssistService != null

    fun destroy() {
        try {
            Shizuku.unbindUserService(userServiceArgs, serviceConnection, true)
            Shizuku.removeRequestPermissionResultListener(permissionListener)
            Log.d(TAG, "Shizuku 已销毁")
        } catch (e: Exception) {
            Log.e(TAG, "销毁失败", e)
        }
    }
}