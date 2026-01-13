package com.yike.jarvis.core.shizuku.service

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import com.yike.jarvis.BuildConfig
import com.yike.jarvis.core.shizuku.tool.UserService
import com.yike.jarvis.di.entrypoints.ShizukuEntryPoint
import com.yike.jarvis.shizuku.IUserService
import com.yike.jarvis.utils.ScriptUtils
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import rikka.shizuku.Shizuku
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shizuku APP 最重要的功能是作为中介，接收应用请求，发送到系统服务器，并返回结果，相当于服务端。
 * Shizuku 库 的作用是在应用中，发指令给 Shizuku APP，相当于客户端。
 * 使用流程：1. 检查 Shizuku APP 授权
 * 2. 绑定 UserService
 * 3. 返回 shizukuService 实例，然后使用实例调用 UserService 中实现的功能，例如 instance.openApp
 * */

@Singleton
class ShizukuService @Inject constructor(@ApplicationContext val context: Context) {

    private val shizukuRepository by lazy {
        EntryPointAccessors.fromApplication(
            context,
            ShizukuEntryPoint::class.java
        ).shizukuRepository()
    }

    private val TAG = "ShizukuManager"
    private val REQUEST_CODE = 100
    private val SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api"
    private lateinit var ShizukuToolInstance: IUserService


    // ------------------ Shizuku 使用流程 1： 检查 Shizuku 运行状态 ------------------
    fun isShizukuAppRunning(): Boolean {
        val connected = Shizuku.pingBinder()

        val newStatus = if (connected) {
            shizukuRepository.shizukuStatus.value.copy(
                running = ShizukuRunningState.RUNNING
            )
        } else {
            // Shizuku 没运行 → 授权和绑定都不可能成立
            shizukuRepository.shizukuStatus.value.copy(
                running = ShizukuRunningState.NOT_RUNNING,
                grant = ShizukuGrantState.NOT_GRANTED,
                bind = ShizukuBindState.NOT_BINDED
            )
        }

        shizukuRepository.updateShizukuStatus(newStatus)

        return connected
    }


    // ------------------ Shizuku 使用流程 2： 检查 APP 授权状态 ------------------

    fun isShizukuGranted(): Boolean {
        return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { req, grant ->
        if (req == REQUEST_CODE && grant == PackageManager.PERMISSION_GRANTED) {
            // 更新授权状态为 GRANTED
            shizukuRepository.updateShizukuStatus(
                shizukuRepository.shizukuStatus.value.copy(
                    grant = ShizukuGrantState.GRANTED
                )
            )

            // 授权成功后尝试绑定服务
            bindUserService()

        } else {
            // 更新授权状态为 NOT_GRANTED
            shizukuRepository.updateShizukuStatus(
                shizukuRepository.shizukuStatus.value.copy(
                    running = ShizukuRunningState.RUNNING,
                    grant = ShizukuGrantState.NOT_GRANTED,
                    bind = ShizukuBindState.NOT_BINDED   // 授权失败 → 绑定必然失败
                )
            )
            Log.e(TAG, "用户已拒绝 Shizuku 权限。")
        }
    }


    // ------------------ Shizuku 使用流程 3： 绑定 UserService ------------------

    fun bindUserService() {
        try {
            Log.i(TAG, "正在绑定 UserService...")
            Shizuku.bindUserService(userServiceArgs, serviceConnection)
        } catch (e: Exception) {
            Log.i(TAG, "Bind UserService 失败...")
            shizukuRepository.updateShizukuStatus(
                shizukuRepository.shizukuStatus.value.copy(
                    bind = ShizukuBindState.NOT_BINDED
                )
            )
        }

    }

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name)
    )
        .daemon(false)
        .processNameSuffix("assist")
        .debuggable(BuildConfig.DEBUG)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i(TAG, "Shizuku 服务连接成功")
            ShizukuToolInstance = IUserService.Stub.asInterface(service)
            shizukuRepository.attachUserServiceInstance(ShizukuToolInstance)
            shizukuRepository.updateShizukuStatus(
                shizukuRepository.shizukuStatus.value.copy(
                    running = ShizukuRunningState.RUNNING,
                    grant = ShizukuGrantState.GRANTED,
                    bind = ShizukuBindState.BINDED
                )
            )

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e(TAG, "Shizuku 服务断开连接")
            shizukuRepository.attachUserServiceInstance(null)
            shizukuRepository.updateShizukuStatus(
                shizukuRepository.shizukuStatus.value.copy(
                    running = ShizukuRunningState.RUNNING,
                    grant = ShizukuGrantState.GRANTED,
                    bind = ShizukuBindState.NOT_BINDED
                )
            )
        }
    }

    // ------------------ Shizuku 初始化流程 ------------------
    fun initShizukuTool() {


        if (!isShizukuAppRunning()) {
            ScriptUtils.openApp(SHIZUKU_PACKAGE_NAME)
            return
        }

        if (BuildConfig.FORCE_SHIZUKU_REAUTH || !isShizukuGranted()) {
            requestShizukuPermission()
            return
        }

        bindUserService()
    }

    fun requestShizukuPermission() {
        Shizuku.addRequestPermissionResultListener(permissionListener)
        Shizuku.requestPermission(REQUEST_CODE)
    }

}