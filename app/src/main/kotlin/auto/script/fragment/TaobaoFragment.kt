package auto.script.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import auto.script.R
import auto.script.executor.TaobaoExecutor
import auto.script.service.AutomationBridgeService
import auto.script.service.AutomationService
import auto.script.shizuku.ShizukuManager
import auto.script.utils.ScriptUtils

class TaobaoFragment : Fragment() {

    private val TAG = "TaobaoFragment"
    private lateinit var taobaoExecutor: TaobaoExecutor
    private var isBound = false

    // ------------------ Fragment 生命周期 ------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_taobao, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 检查 A11yService 权限
        view.findViewById<Button>(R.id.check_a11y).setOnClickListener {
            val a11yService = ScriptUtils.isAccessibilityServiceEnabled(
                requireContext(),
                AutomationService::class.java
            )
            if (a11yService) {
                Log.i(TAG, "A11yService 已开启 。")
            } else {
                Log.i(TAG, "A11yService 未开启。")
                ScriptUtils.openAccessibilitySettings(requireContext())
            }
        }

        // 绑定 Shizuku 按钮事件
        view.findViewById<Button>(R.id.check_shizuku).setOnClickListener {
            val checkPermissionResult = ShizukuManager.checkShizukuPermission()
            if (checkPermissionResult) {
                Log.i(TAG, "Shizuku 已授权应用 ADB 权限，开始绑定 UserService 服务。")
            } else {
                Log.i(TAG, "Shizuku 未授权应用 ADB 权限，请授权。")
            }
        }

        // bindService
        view.findViewById<Button>(R.id.bindService).setOnClickListener {
            bindService()
        }

        // 启动服务按钮事件
        val startButton = view.findViewById<Button>(R.id.tb_start_service)
        startButton.setOnClickListener {
            startAutomation()
        }

        // 停止服务按钮事件
        val stopButton = view.findViewById<Button>(R.id.tb_stop_service)
        stopButton.setOnClickListener {
            stopAutomation()
        }
    }


    override fun onStop() {
        super.onStop()
        // 在 Fragment 停止时解绑 Service
        unBindService()
    }

    // ------------------ ServiceConnection 实现 ------------------
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // 连接成功，获取 Service 实例
            val binder = service as AutomationBridgeService.LocalBinder
            taobaoExecutor = binder.getTaobaoExecutor()
            isBound = true
            Log.d(TAG, "AutomationBridgeService 服务已连接。")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Service 进程意外终止（被系统杀死），这是关键的断开点
            isBound = false

            Log.d(TAG, "无障碍服务已断开！")
            // 提示用户或自动重试绑定/重新开启 A11y 服务
        }
    }

    // ------------------ 绑定与解绑 ------------------

    private fun bindService() {
        val intent = Intent(activity, AutomationBridgeService::class.java).apply {
            // 使用内部 action 区分，与 Service 的 onBind() 对应
            action = "auto.script.taobao"
        }

        // BIND_AUTO_CREATE：如果 Service 未运行，会先创建它
        activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unBindService() {
        if (isBound) {
            activity?.unbindService(connection)
            isBound = false

            Log.d(TAG, "Service unbound by Fragment.")
        }
    }

    // ------------------ 任务执行与恢复 ------------------

    private fun startAutomation() {
        taobaoExecutor.startAutomation()
    }

    private fun stopAutomation() {
        taobaoExecutor.stopAutomation()
    }

    private fun resumeAutomation() {
        // TODO: 从 SharedPreferences 或数据库读取状态
//        val taskState = "TASK_COMPLETED_1_WAITING_2" // 假设上次任务中断于此
//
//        if (taskState == "TASK_COMPLETED_1_WAITING_2") {
//            Log.i(TAG, "Resuming interrupted task...")
//            taobaoExecutor?.startAutomation(taskId = 2, data = null)
//        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        ShizukuManager.destroy()
    }


}