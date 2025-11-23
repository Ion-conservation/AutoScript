package auto.script.activity


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import auto.script.R
import auto.script.executor.CloudmusicExecutor
import auto.script.executor.TaobaoExecutor
import auto.script.service.AutomationBridgeService
import auto.script.service.AutomationService
import auto.script.shizuku.ShizukuManager
import auto.script.utils.ScriptUtils
import auto.script.viewmodel.AutomationViewModel


class MainActivity : AppCompatActivity() {

    lateinit var shizukuManager: ShizukuManager

    private lateinit var cloudmusicExecutor: CloudmusicExecutor
    private lateinit var taobaoExecutor: TaobaoExecutor

    private val viewModel: AutomationViewModel by viewModels()
    private var TAG = "MainActivity"


    // 公共按钮
    private lateinit var A11yServiceButton: Button
    private lateinit var ShizukuButton: Button
    private lateinit var UserServiceButton: Button

    // CloudMusic 按钮
    private lateinit var CloudmusicStartButton: Button
    private lateinit var CloudmusicStopButton: Button

    // Taobao 按钮
    private lateinit var TaobaoStartButton: Button
    private lateinit var TaobaoStopButton: Button

    private var isBridegServiceBound = false

    private var isUserServiceBind = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shizukuManager.init(this)

        setStyle() // 设置 APP 样式

        initButton() // 初始化按钮

        initButtonListener() // 按钮绑定事件

        observeButtonStatus() // 监听按钮状态

        setA11yServiceButtonText() // 初始化 A11yService 按钮文本

        setShizukuButtonText() // 初始化 Shizuku 按钮文本

        setUserServiceButtonText()

        checkUserService() // 检查 UserService
    }

    private fun setStyle() {
        // 让内容延伸到状态栏
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 控制状态栏文字颜色（亮色/暗色）
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true  // true = 黑字，false = 白字
    }

    private fun initButton() {
        A11yServiceButton = findViewById(R.id.check_a11y)
        ShizukuButton = findViewById(R.id.check_shizuku)
        UserServiceButton = findViewById(R.id.bindService)

        CloudmusicStartButton = findViewById(R.id.cloudmusic_start_service)
        CloudmusicStopButton = findViewById(R.id.cloudmusic_stop_service)

        TaobaoStartButton = findViewById(R.id.tb_start_service)
        TaobaoStopButton = findViewById(R.id.tb_stop_service)
    }

    private fun observeButtonStatus() {

        viewModel.a11yStatus.observe(this, Observer { connected ->
            Log.d(TAG, "A11yService 状态已更新：$connected")
            A11yServiceButton.text = "A11yService: " + if (connected) "已连接 ✅" else "未连接 ❌"
        })

        viewModel.shizukuStatus.observe(this, Observer { connected ->
            ShizukuButton.text = "Shizuku: " + if (connected) "已连接 ✅" else "未连接 ❌"
        })

        viewModel.bindServiceStatus.observe(this, Observer { connected ->
            UserServiceButton.text = "Bind Service: " + if (connected) "已连接 ✅" else "未连接 ❌"
        })
    }

    private fun initButtonListener() {
        // 公共按钮逻辑
        A11yServiceButton.setOnClickListener {
            if (!checkA11yServicePermission()) {
                ScriptUtils.openAccessibilitySettings(this)
            } else {
                showToast("A11yService 已打开 ✅")
            }
        }

        ShizukuButton.setOnClickListener {
            if (shizukuManager.isShizukuRunning()) {
                showToast("Shizuku 已运行 ✅")
            } else {
                val intent = packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                if (intent != null) {
                    startActivity(intent)
                }
            }
        }

        UserServiceButton.setOnClickListener {
            bindService()
        }
        // CloudMusic 按钮逻辑
        CloudmusicStartButton.setOnClickListener {
            cloudmusicExecutor.startAutomation()
        }

        CloudmusicStopButton.setOnClickListener {
            cloudmusicExecutor.stopAutomation()
        }

        // Taobao 按钮逻辑
        TaobaoStartButton.setOnClickListener {
            taobaoExecutor.startAutomation()
        }

        TaobaoStopButton.setOnClickListener {
            taobaoExecutor.stopAutomation()
        }
    }


    private fun showToast(msg: String) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    // ------------------ ServiceConnection 实现 ------------------
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // 连接成功，获取 Service 实例
            val binder = service as AutomationBridgeService.LocalBinder
            cloudmusicExecutor = binder.getCloudmusicExecutor()
            isBridegServiceBound = true
            Log.d(TAG, "AutomationBridgeService 服务已连接。")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Service 进程意外终止（被系统杀死），这是关键的断开点
            isBridegServiceBound = false

            Log.d(TAG, "无障碍服务已断开！")
            // 提示用户或自动重试绑定/重新开启 A11y 服务
        }
    }

    // ------------------ 绑定与解绑 ------------------

    private fun bindService() {
        val intent = Intent(this@MainActivity, AutomationBridgeService::class.java).apply {
            // 使用内部 action 区分，与 Service 的 onBind() 对应
            action = "auto.script.cloudmusic"
        }

        // BIND_AUTO_CREATE：如果 Service 未运行，会先创建它
        this@MainActivity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unBindService() {
        if (isBridegServiceBound) {
            this@MainActivity?.unbindService(connection)
            isBridegServiceBound = false

            Log.d(TAG, "Service unbound by Fragment.")
        }
    }

    private fun checkA11yServicePermission(): Boolean {
        return ScriptUtils.isAccessibilityServiceEnabled(
            this,
            AutomationService::class.java
        )
    }

    private fun setA11yServiceButtonText() {
        val a11yService = checkA11yServicePermission()
        if (a11yService) {
            A11yServiceButton.text = "A11yService 已打开 ✅"
        } else {
            A11yServiceButton.text = "A11yService 未打开 ❌"
        }
    }

    private fun setShizukuButtonText() {
        if (checkShizukuIsRunning()) {
            ShizukuButton.text = "Shizuku 已运行 ✅"
            if (checkShizukuPermission()) {
                ShizukuButton.text = "Shizuku 已授权应用 ✅"
            } else {
                ShizukuButton.text = "Shizuku 未授权应用 ❌"
            }
        } else {
            ShizukuButton.text = "Shizuku 未运行 ❌"
        }
    }

    private fun checkShizukuIsRunning(): Boolean {
        return shizukuManager.isShizukuRunning()
    }

    private fun checkShizukuPermission(): Boolean {
        return shizukuManager.checkShizukuPermission()
    }


    private fun setUserServiceButtonText() {
        if (checkUserService()) {
            UserServiceButton.text = "Bind Service 已连接 ✅"
        } else {
            UserServiceButton.text = "Bind Service 未连接 ❌"
        }
    }

    private fun checkUserService(): Boolean {
        return isUserServiceBind
    }

}