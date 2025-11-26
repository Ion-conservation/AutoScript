package auto.script.activity

import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import auto.script.R
import auto.script.executor.CloudmusicExecutor
import auto.script.executor.ExecutorRepo
import auto.script.executor.TaobaoExecutor
import auto.script.shizuku.ShizukuManager
import auto.script.utils.ScriptUtils
import auto.script.viewmodel.AutomationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: AutomationViewModel by viewModels()

    @Inject
    lateinit var shizukuManager: ShizukuManager

    @Inject
    lateinit var bindServiceRepo: BindServiceRepo

    @Inject
    lateinit var executorRepo: ExecutorRepo

    @Inject
    lateinit var taobaoExecutor: TaobaoExecutor

    @Inject
    lateinit var cloudmusicExecutor: CloudmusicExecutor
    private lateinit var bindServiceHelper: BindService

    private var TAG = "MainActivity"

    // 公共按钮
    private lateinit var A11yServiceButton: Button
    private lateinit var ShizukuButton: Button
    private lateinit var BindServiceButton: Button
//    private lateinit var BridgeServiceButton: Button


    // CloudMusic 按钮
    private lateinit var CloudmusicStartButton: Button
    private lateinit var CloudmusicStopButton: Button

    // Taobao 按钮
    private lateinit var TaobaoStartButton: Button
    private lateinit var TaobaoStopButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setStyle() // 设置 APP 样式

        initButton() // 初始化按钮

        initButtonListener() // 按钮绑定事件

        initBindHelper()




        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.shizukuUiState.collect { ui ->
                        ShizukuButton.text = ui.text
                        ShizukuButton.isEnabled = ui.buttonEnabled
                    }
                }

                launch {
                    viewModel.a11yServiceUiState.collect { ui ->
                        A11yServiceButton.text = ui.text
                        A11yServiceButton.isEnabled = ui.buttonEnabled
                    }
                }

//                launch {
//                    viewModel.bridgeServiceUiState.collect { ui ->
//                        BridgeServiceButton.text = ui.text
//                        BridgeServiceButton.isEnabled = ui.buttonEnabled
//                    }
//                }

                launch {
                    viewModel.bindServiceUiState.collect { ui ->
                        BindServiceButton.text = ui.text
                        BindServiceButton.isEnabled = ui.buttonEnabled
                    }
                }


            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bindServiceHelper.unBind(context = this)
    }


    private fun initBindHelper() {
        bindServiceHelper = BindService()
        bindServiceHelper.bind(context = this)
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
        BindServiceButton = findViewById(R.id.bindService)
//        BridgeServiceButton = findViewById(R.id.bridgeService)

        CloudmusicStartButton = findViewById(R.id.cloudmusic_start_service)
        CloudmusicStopButton = findViewById(R.id.cloudmusic_stop_service)

        TaobaoStartButton = findViewById(R.id.tb_start_service)
        TaobaoStopButton = findViewById(R.id.tb_stop_service)
    }


    private fun initButtonListener() {
        // 公共按钮逻辑
        A11yServiceButton.setOnClickListener {
            ScriptUtils.openAccessibilitySettings(this)
        }

        ShizukuButton.setOnClickListener {
            if (shizukuManager.isRunning()) {
                shizukuManager.start()
            } else {
                val intent = packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                if (intent != null) {
                    startActivity(intent)
                }
            }
        }

        BindServiceButton.setOnClickListener {
            initBindHelper()
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


}