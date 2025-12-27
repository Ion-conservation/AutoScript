package auto.script.feature.taobao

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import auto.script.A11yService.A11yServiceTool
import auto.script.common.EventTaskHandler
import auto.script.gesture.GestureManager
import auto.script.shizuku.IUserService
import auto.script.utils.ScriptLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaobaoExecutor @Inject constructor() : EventTaskHandler {
    companion object {
        private val handler = Handler(Looper.getMainLooper())

        private const val APP_PACKAGE_NAME = "com.taobao.taobao"

        private var currentState = State.IDLE

        private enum class State {
            IDLE,   // 空闲状态，等待应用启动
            LAUNCHING_APP, // 正在启动 APP，通过 onAccessibilityEvent 进入下一步
            WAIT_TO_CLICK_ACHIEVEMENT_CENTER_BUTTON, // 点击 成就中心 按钮
        }

    }

    // 使用可空的 var，或者一个接口
    private var a11yService: A11yServiceTool? = null

    // 提供一个绑定方法
    fun attachA11yService(service: A11yServiceTool) {
        this.a11yService = service
    }

    // 提供一个解绑方法（防止内存泄漏）
    fun detachA11yService() {
        this.a11yService = null
    }

    var shizukuService: IUserService? = null

    // 提供一个绑定方法
    fun attachShizukuService(service: IUserService) {
        this.shizukuService = service
    }

    // 提供一个解绑方法（防止内存泄漏）
    fun detachShizukuService() {
        this.shizukuService = null
    }


    private val TAG = "淘宝脚本"


    // 创建一个协程作用域，用于执行异步任务
    // 它会与无障碍服务的生命周期绑定
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var gestureManager: GestureManager
    private var userService: IUserService? = null

    private val isRunning = AtomicBoolean(false)


//    override fun onCreate() {
//
//        super.onCreate()
//
////        ShizukuManager.init(this)
//
//        registerBroadcast()
//
//        initGestureManeger()
//    }

//    override fun onServiceConnected() {
//        ScriptLogger.i(TAG, "无障碍服务已连接。")
//        super.onServiceConnected()
//
//
//        // 无障碍服务已连接，现在我们去连接 Shizuku
////        bindShizukuService()
////
//        val currentPackage = rootInActiveWindow?.packageName?.toString()
////
////        ScriptLogger.i(TAG, "currentPackage: $currentPackage")
//        if (currentPackage == "com.android.settings") {
//            val intent = packageManager.getLaunchIntentForPackage("auto.script")
//            intent.let {
//                // 确保 Activity 被带到前台
//                it?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                startActivity(it)
//                ScriptLogger.i(TAG, "正在回到脚本...")
//            }
//        }
//
//    }

//    override fun onAccessibilityEvent(event: AccessibilityEvent) {
//        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
////            ScriptLogger.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED packageName: ${event.packageName}")
////            ScriptLogger.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED className: ${event.className.toString()}")
//
//
//        }
//
//        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            ScriptLogger.d(TAG, "TYPE_WINDOW_STATE_CHANGED packageName: ${event.packageName}")
//            ScriptLogger.d(TAG, "TYPE_WINDOW_STATE_CHANGED className: ${event.className.toString()}")
//
//            if (event.packageName == APP_PACKAGE_NAME && currentState == State.LAUNCHING_APP) {
//                ScriptLogger.i(TAG, "${APP_PACKAGE_NAME} 启动成功。")
//                driveByInnerState(State.WAIT_TO_CLICK_ACHIEVEMENT_CENTER_BUTTON, 2000)
//            }
//
//        }
//
//    }

//    override fun onUnbind(intent: Intent?): Boolean {
//        ScriptLogger.w(TAG, "AccessibilityService onUnbind 被调用")
//
//        // 打印调用栈
//        val stackTrace = Throwable().stackTrace
//        stackTrace.forEach { element ->
//            ScriptLogger.w(TAG, "onUnbind stack: $element")
//        }
//
//        return super.onUnbind(intent)
//    }

//    override fun onInterrupt() {
//        ScriptLogger.w(TAG, "服务被中断。")
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        // 注销广播接收器
//        ScriptBroadcast.unregister("taobao", "start")
//        ScriptBroadcast.unregister("taobao", "stop")
//
//        ShizukuManager.destroy()
//    }

    override fun handleAccessibilityEvent(event: AccessibilityEvent) {

    }

    /**
     * 这是你的工作流的完整实现！
     */
    fun startAutomation() {
        isRunning.set(true)
        // todo：基本配置已完成，剩下具体的工作流程了。
        ScriptLogger.i(TAG, "接收到 startAutomation 1")

//        shizukuService?.openApp(APP_PACKAGE_NAME)

        handler.postDelayed({ handleAchievementCenterButton() }, 2000)


//
//            try {
//                // 工作流 1: 打开 App (如果需要的话，但我们已经在 App 里了)
//                // service.openApp("com.example.targetapp")
//                // delay(1000) // 等待 App 打开
//
//                // 工作流 2 & 3: 获取 XML 并查找节点
//                ScriptLogger.d(TAG, "Workflow 2&3: Getting UI XML and finding node...")
//                val xml = service.getUiXml()
//                if (xml.startsWith("Error:")) {
//                    ScriptLogger.e(TAG, "Failed to get UI XML: $xml")
//                    return@launch
//                }
//
//                // 假设我们要查找一个“登录”按钮
//                val targetBounds = findNodeBounds(xml, "登录", "login_button_desc")
//
//                if (targetBounds == null) {
//                    ScriptLogger.w(TAG, "Could not find '登录' button on screen.")
//                    // 找不到按钮？也许需要滑动
//                    // 工作流 5: 模拟滑动 (从屏幕中间向下滑动)
//                    ScriptLogger.d(TAG, "Workflow 5: Swiping down...")
//                    service.swipe(500, 1500, 500, 500, 300)
//                    delay(1000)
//
//                    // 再次尝试
//                    val xmlAfterSwipe = service.getUiXml()
//                    val boundsAfterSwipe =
//                        findNodeBounds(xmlAfterSwipe, "登录", "login_button_desc")
//
//                    if (boundsAfterSwipe != null) {
//                        ScriptLogger.d(TAG, "Workflow 4: Found node after swipe! Tapping...")
//                        service.tap(boundsAfterSwipe.centerX(), boundsAfterSwipe.centerY())
//                    }
//
//                } else {
//                    // 找到了按钮！
//                    // 工作流 4: 模拟点击
//                    ScriptLogger.d(TAG, "Workflow 4: Found node! Tapping...")
//                    service.tap(targetBounds.centerX(), targetBounds.centerY())
//                }
//
//                // 等待点击生效
//                delay(2000)
//
//                // 工作流 6: 模拟返回
//                ScriptLogger.d(TAG, "Workflow 6: Simulating back press...")
//                service.back()
//
//                ScriptLogger.d(TAG, "Automation cycle complete.")
//
//            } catch (e: Exception) {
//                ScriptLogger.e(TAG, "Automation workflow failed", e)
//            }

    }


//    private fun launchApp() {
//        val intent = packageManager.getLaunchIntentForPackage(APP_PACKAGE_NAME)
//
//        if (intent != null) {
//            intent.let {
//                // 必须的标志：在新任务栈中启动
//                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                // 确保 Activity 被带到前台
//                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                // 如果任务已存在，将其重置并带到前台
//                it.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
//
//                // 步骤 1：启动 APP
//                driveByOuterState(State.LAUNCHING_APP)
//                startActivity(it)
//                ScriptLogger.i(TAG, "步骤 1：启动淘宝...")
//            }
//        } else {
//            ScriptLogger.e(
//                TAG,
//                "无法获取网易云音乐的启动 Intent。请检查 APP_PACKAGE_NAME 是否正确：$APP_PACKAGE_NAME"
//            )
//        }
//    }

//    private fun handleStateLogic() {
//        if (rootInActiveWindow == null) {
//            ScriptLogger.w(TAG, "Root node is null, cannot proceed.")
//            return
//        }
//
//        when (currentState) {
//
//
//            State.WAIT_TO_CLICK_ACHIEVEMENT_CENTER_BUTTON -> handleAchievementCenterButton()
//
//
//            else -> {
//                /* 其它状态由内部逻辑驱动，不直接由事件触发 */
//            }
//        }
//    }


    private fun handleAchievementCenterButton() {
//        val xml = ShizukuManager.withService { getUiXml() } ?: ""
//        if (xml.isEmpty()) {
//            ScriptLogger.e(TAG, "Failed to get UI XML")
//            return
//        }

//        val targetBounds = ScriptUtils.findNodeBounds(xml, "", "成就中心")
//        if (targetBounds == null) {
//            ScriptLogger.w(TAG, "Could not find '成就中心' button on screen.")
//            return
//        }
//
//        val success = ShizukuManager.withService {
//            tap(targetBounds.centerX(), targetBounds.centerY())
//            true
//        } ?: false
//
//        if (!success) {
//            ScriptLogger.e(TAG, "Tap failed, service not available")
//        }
    }


//    fun findByClassAndContentDesc(
//        root: AccessibilityNodeInfo? = rootInActiveWindow,
//        targetClass: String,
//        targetDesc: String
//    ): AccessibilityNodeInfo? {
//        if (root == null) return null
//
//        val classMatch = root.className?.toString() == targetClass
//        val descMatch = root.contentDescription?.toString() == targetDesc
//
//        if (classMatch && descMatch) {
//            return root
//        }
//
//        for (i in 0 until root.childCount) {
//            val child = root.getChild(i)
//            val result = findByClassAndContentDesc(child, targetClass, targetDesc)
//            if (result != null) return result
//        }
//
//        return null
//    }


    fun stopAutomation() {
        isRunning.set(false)
        ScriptLogger.i(TAG, "接收到 stopAutomation")
        currentState = State.IDLE
        handler.removeCallbacksAndMessages(null) // 清除所有延迟任务
    }

//    private fun driveByInnerState(newState: State, delay: Long = 1000L) {
//        // 状态改变前，清除所有延时任务
//        handler.removeCallbacksAndMessages(null) // 清除所有延迟任务
//        currentState = newState
//        ScriptLogger.d(TAG, "内部状态改变 $newState，触发事件 handleStateLogic")
//        handler.postDelayed({ handleStateLogic() }, delay)
//    }

//    private fun driveByOuterState(newState: State) {
//        currentState = newState
//        ScriptLogger.d(TAG, "等待 TYPE_WINDOW_STATE_CHANGED 改变，触发事件")
//    }
//
//    private fun executeWithTimeoutRetry(
//        description: String = "",
//        timeoutMills: Long = 5000L,
//        delayMills: Long = 500L,
//        checkBeforeAction: () -> Unit = {},
//        findAction: () -> AccessibilityNodeInfo?,
//        executeAction: (node: AccessibilityNodeInfo?) -> Unit,
//    ) {
//        if (description.isNotEmpty()) {
//            ScriptLogger.i(TAG, description)
//        }
//
//        val startTime = System.currentTimeMillis()
//
//        val retryRunnable = object : Runnable {
//            override fun run() {
//                // 专门用于检查各种弹窗
//                checkBeforeAction()
//
//                val result = findAction()
//                if (result != null) {
//                    handler.removeCallbacks(this)
//                    executeAction(result)
//                } else {
//                    val elapsed = System.currentTimeMillis() - startTime
//                    if (elapsed < timeoutMills) {
//                        handler.postDelayed(this, delayMills)
//                    } else {
//                        handler.removeCallbacks(this)
//                        executeAction(null)
//                    }
//                }
//            }
//        }
//        handler.post(retryRunnable)
//    }
//
//    private fun registerBroadcast() {
//        ScriptLogger.i(TAG, "创建广播接收器。")
//
//        ScriptBroadcast.register("taobao", "start") { intent ->
//            startAutomation()
//        }
//
//        ScriptBroadcast.register("taobao", "stop") { _ ->
//            stopAutomation()
//        }
//    }
//
//    private fun initGestureManeger() {
//        ScriptLogger.i(TAG, "初始化 GestureManeger 实例。")
//        gestureManager = GestureManager(this)
//    }

}