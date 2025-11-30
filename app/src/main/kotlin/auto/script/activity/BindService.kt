package auto.script.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import auto.script.service.AutomationService
import auto.script.utils.ScriptLogger
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BindService @Inject constructor() {

    private val TAG = "BindService"

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // 连接成功，获取 Service 实例
            ScriptLogger.d(TAG, "AutomationService 服务已连接。")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            ScriptLogger.d(TAG, "Bind Service 已断开！")
        }
    }

    fun bind(context: Context) {
        ScriptLogger.d(TAG, "Service binding...")
        val intent = Intent(context, AutomationService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unBind(context: Context) {
        context.unbindService(connection)
        ScriptLogger.d(TAG, "Service unbound by Fragment.")
    }
}