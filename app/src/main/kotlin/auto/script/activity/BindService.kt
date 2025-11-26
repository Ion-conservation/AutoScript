package auto.script.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import auto.script.service.AutomationService
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BindService @Inject constructor() {

    private val TAG = "BindService"
    
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // 连接成功，获取 Service 实例
            Log.d(TAG, "AutomationService 服务已连接。")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Bind Service 已断开！")
        }
    }

    fun bind(context: Context) {
        Log.d(TAG, "Service binding...")
        val intent = Intent(context, AutomationService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unBind(context: Context) {
        context.unbindService(connection)
        Log.d(TAG, "Service unbound by Fragment.")
    }
}