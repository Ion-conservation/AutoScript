package auto.script

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import auto.script.utils.FindEle

class ScriptReceiver : BroadcastReceiver() {
    val TAG = "scriptReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "收到广播。")
        val action = intent?.action
        val scriptName = intent?.getStringExtra("scriptName")
        if (action !== "auto.script") {
            Log.i(TAG, "收到未知命令。")
            return
        }
        // 重点：检查服务实例是否已经准备好
        if (!CloudMusicService.isServiceInstanceReady()) {
            Log.e(TAG, "服务尚未连接或未初始化，无法执行命令！")
            // 可以在这里提示用户或尝试启动服务
            return
        }

        if (scriptName == "start_script") {
            Log.i(TAG, "收到启动命令。")
            CloudMusicService.startAutomation()
        } else if (scriptName == "stop_script") {
            Log.i(TAG, "收到停止命令。")
            CloudMusicService.stopAutomation()
        }
    }
}

class CloudMusicService : AccessibilityService() {

    companion object {
        lateinit var instance: CloudMusicService
        private const val TAG = "网易云音乐脚本"
        private const val APP_PACKAGE_NAME = "com.netease.cloudmusic"
        private var hadClickFreeListenButton = false // 标记是否第一次，避免重复滚动等其他操作
        private var isRunning = false
        private val handler = Handler(Looper.getMainLooper())
        private var isScrolling = false // 防止重复滚动

        // 使用一个简单的状态机来管理自动化流程
        private var currentState = State.IDLE
        private var currentActivity = ""

        // 自动化流程的状态枚举
        private enum class State {
            IDLE,                   // 空闲状态，等待应用启动
            LAUNCHING_APP, //  正在启动 APP，通过 onAccessibilityEvent 进入下一步
            WAIT_TO_CLICK_SKIP_BUTTON, // 等待点击 跳过 按钮
            WAIT_TO_CLICK_FREE_BUTTON,   // 点击 “免费听” 按钮
            FIND_SCROLL_CONTAINER, // 查找滚动容器，从下向上滚动 300 距离
            CLICK_VIDEO_AD, // 找到 ”看视频，点亮拼图“ 按钮，并点击
            HANDLE_AD_TITLE, // 点击广告，进入详情页

            // 看15秒后点击
            CLICK_AD_AFTER_15_SECOND,
            CHECK_IF_CANCEL_BUTTON,
            CHECK_IF_CANCEL_BUTTON_AND_WAIT,
            RETURN_TO_APP,
            CLICK_AD_IMMEDIATE

        }

        fun startAutomation() {
            instance.launchApp()
        }


        fun stopAutomation() {
            Log.i(TAG, "接收到外部停止信号，重置自动化流程。")
            instance.resetState()
        }

        // 重点：添加一个检查方法
        fun isServiceInstanceReady(): Boolean {
            // 检查 'instance' 是否已经在本类中被初始化
            return ::instance.isInitialized
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onServiceConnected() {
        instance = this
        Log.i(TAG, "服务已连接，等待静态广播...")

        val intent = packageManager.getLaunchIntentForPackage("auto.script")
        intent.let {
            // 确保 Activity 被带到前台
            it?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(it)
            Log.i(TAG, "正在回到脚本...")
        }
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

//            Log.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED packageName: ${event.packageName}")
//            Log.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED className: ${event.className.toString()}")

            if (
                currentState == State.LAUNCHING_APP
                && event.packageName.toString() == APP_PACKAGE_NAME
            ) {
                Log.i(TAG, "步骤 1 结果：网易云音乐 APP 启动成功。 ${event.eventType}")
                driveByInnerState(State.WAIT_TO_CLICK_SKIP_BUTTON)
            }
        }


        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            Log.d(TAG, "TYPE_WINDOW_STATE_CHANGED packageName: ${event.packageName}")
            Log.d(TAG, "TYPE_WINDOW_STATE_CHANGED className: ${event.className.toString()}")

            currentActivity = event.className.toString()
        }

    }


    override fun onInterrupt() {
        Log.w(TAG, "服务被中断。")
        resetState()
    }


    /**
     * 核心状态处理逻辑
     */
    private fun handleStateLogic() {
        if (rootInActiveWindow == null) {
            Log.w(TAG, "Root node is null, cannot proceed.")
            return
        }

        when (currentState) {
            State.WAIT_TO_CLICK_SKIP_BUTTON -> handleClickSkipButton()
            // 3. 进入 APP 首页，查找 ”免费听“ 按钮，并点击
            State.WAIT_TO_CLICK_FREE_BUTTON -> handleClickFreeListen()
            // 4. 找到滚动容器，从下到上滚动 300 距离
            State.FIND_SCROLL_CONTAINER -> handleFindScrollContainerAndScrollDelay()
            // 5. 找到 ”看视频，点亮拼图“ 按钮，并点击
            State.CLICK_VIDEO_AD -> handleClickVideoAD()
            // 6. 点击广告，进入详情页
            State.HANDLE_AD_TITLE -> handleAdTitle()

            // 看15秒后点击
            State.CLICK_AD_AFTER_15_SECOND -> handleClickADAfter15Second()
            // 检查取消按钮
            State.CHECK_IF_CANCEL_BUTTON -> handleCheckCancelButton(2000L)
            State.CHECK_IF_CANCEL_BUTTON_AND_WAIT -> handleCheckCancelButton(10000L)
            // 结合 activity 返回 APP
            State.RETURN_TO_APP -> handleReturnApp()


            // 点击跳转APP停留10秒
            State.CLICK_AD_IMMEDIATE -> handleClickADImmediateAndWait10Second()
            else -> { /* 其它状态由内部逻辑驱动，不直接由事件触发 */
            }
        }
    }


    private fun launchApp() {
        val intent = packageManager.getLaunchIntentForPackage(APP_PACKAGE_NAME)

        if (intent != null) {
            intent.let {
                // 必须的标志：在新任务栈中启动
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // 确保 Activity 被带到前台
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                // 如果任务已存在，将其重置并带到前台
                it.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

                // 步骤 1：启动 APP
                driveByOuterState(State.LAUNCHING_APP)
                startActivity(it)
                Log.i(TAG, "步骤 1：启动网易云音乐...")
            }
        } else {
            Log.e(
                TAG,
                "无法获取网易云音乐的启动 Intent。请检查 APP_PACKAGE_NAME 是否正确：$APP_PACKAGE_NAME"
            )
        }
    }

    private fun findSkipButton(): AccessibilityNodeInfo? {
        return rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.netease.cloudmusic:id/skipBtn")
            .firstOrNull()
    }

    private fun handleClickSkipButton() {
        executeWithTimeoutRetry(
            description = "步骤 2 ：查找 APP 启动广告的 ‘跳过’ 按钮并点击。",
            findAction = ::findSkipButton,
            executeAction = { button ->
                if (button != null) {
                    Log.i(TAG, "步骤 2 结果：找到 App 启动广告 '跳过' 按钮，尝试点击。")
                    button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    driveByInnerState(State.WAIT_TO_CLICK_FREE_BUTTON, 1000)
                } else {
                    Log.i(TAG, "步骤 2 结果：找不到 App 启动广告 '跳过' 按钮，等待下一个状态。")
                    driveByInnerState(State.WAIT_TO_CLICK_FREE_BUTTON, 2000)
                }
            },
        )
    }

    private fun findTabAndReturnYouNeed(tabYouNeed: String): AccessibilityNodeInfo? {
        val containerResourceId = "com.netease.cloudmusic:id/tl_custom_theme"
        val horizontalScrollView =
            rootInActiveWindow.findAccessibilityNodeInfosByViewId(containerResourceId)
                ?.firstOrNull()
                ?: return null

        for (i in 0 until horizontalScrollView.childCount) {
            val item = horizontalScrollView.getChild(i)
            val textView = findTextView(item)
            if (textView?.text?.toString().orEmpty() == tabYouNeed) {
                return item
            }
        }
        return null
    }

    private fun checkDialogBeforeClickFreeListenButton() {
        val dialogResourceId = "com.netease.cloudmusic:id/dsl_dialog_root"
        val node = rootInActiveWindow.findAccessibilityNodeInfosByViewId(dialogResourceId)
            ?.firstOrNull()
        if (node != null) {
            Log.i(TAG, "找到弹窗")
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private fun handleClickFreeListen() {

        // 如果已经执行过，就不要再点击免费听了，直接点击 “看视频，点亮拼图”
        if (hadClickFreeListenButton) {
            driveByInnerState(State.CLICK_VIDEO_AD)
            return
        }

        executeWithTimeoutRetry(
            description = "查找 ‘免费听’ 按钮。",
            checkBeforeAction = ::checkDialogBeforeClickFreeListenButton,
            findAction = {
                findTabAndReturnYouNeed("免费听")
            },
            executeAction = { listenFreeButton ->
                if (listenFreeButton != null) {
                    Log.i(TAG, "找到 ‘免费听’ 按钮，尝试点击")
                    listenFreeButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    driveByInnerState(State.FIND_SCROLL_CONTAINER, 2000)
                } else {
                    Log.i(TAG, "查找 ‘免费听’ 按钮失败，退出程序。")
                    stopAutomation()
                }

            }
        )

    }


    private fun scrollContainer() {
        Log.i(TAG, "找到滚动节点，从下向上滚动 300 距离")
        isScrolling = true // 标记正在滚动

        val gestureBuilder = GestureDescription.Builder()
        val path = Path().apply {
            moveTo(500f, 1000f) // 起点（屏幕下方）
            lineTo(500f, 700f)  // 终点（向上滑动 300）
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        gestureBuilder.addStroke(stroke)

        dispatchGesture(
            gestureBuilder.build(),
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription) {
                    super.onCompleted(gestureDescription)
                    Log.i(TAG, "滚动手势完成，2 秒后点击 ‘看视频，点亮拼图’。")

                    isScrolling = false // 立即解除isScrolling标记
                    // 延迟等待UI更新，然后进入下一步
                    driveByInnerState(State.CLICK_VIDEO_AD, 2000)
                }

                override fun onCancelled(gestureDescription: GestureDescription) {
                    super.onCancelled(gestureDescription)
                    Log.w(TAG, "滚动手势被取消。")

                    isScrolling = false // 立即解除标记
                    driveByInnerState(State.CLICK_VIDEO_AD, 2000)
                }
            },
            null
        )
    }

    private fun handleFindScrollContainerAndScrollDelay() {

        if (isScrolling) {
            Log.d(TAG, "正在滚动中，忽略重复滚动请求。")
            return
        }

        scrollContainer()

    }

    private fun checkIfAllDone(): Boolean {
        val node = FindEle.findNodeByRawIdWithTextCheck(
            root = rootInActiveWindow,
            resourceId = "DC_Button",
            targetTexts = listOf("已全部点亮，明天再来")
        )
        if (node != null) {
            Log.i(TAG, "已全部点亮，明天再来")
            stopAutomation()
            return true
        }
        return false
    }

    private fun findPlayVideoButton(): AccessibilityNodeInfo? {
        val node = FindEle.findNodeByRawIdWithTextCheck(
            root = rootInActiveWindow,
            resourceId = "DC_Button",
            targetTexts = listOf("看视频，点亮拼图")
        )
        if (node != null) {
            Log.i(TAG, "找到 DC_Button，包含目标文本，clickable=${node.isClickable}")
            return node
        } else {
            Log.e(TAG, "看视频，点亮拼图")
            return null
        }
    }

    private fun handleClickVideoAD() {

        if (checkIfAllDone()) return

        executeWithTimeoutRetry(
            description = "查找 ‘看视频，点亮拼图’ 按钮。",
            findAction = ::findPlayVideoButton,
            executeAction = { playVideoButton ->
                if (playVideoButton != null) {
                    Log.i(TAG, "找到 ‘看视频，点亮拼图’ 按钮，尝试点击。")
                    playVideoButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    driveByInnerState(State.HANDLE_AD_TITLE, 1000)
                } else {
                    Log.i(TAG, "找不到 ‘看视频，点亮拼图’ 按钮，退出程序。")
                    stopAutomation()
                }

            }

        )
    }

    private fun handleAdTitle() {
        executeWithTimeoutRetry(
            description = "查找广告弹窗的文本，判断如何处理。",
            findAction = {
                val adTitleResourceId = "com.netease.cloudmusic:id/tv_ad_bottom_enhance_main_title"
                rootInActiveWindow.findAccessibilityNodeInfosByViewId(adTitleResourceId)
                    .firstOrNull()
            },
            executeAction = { titleNode ->
                if (titleNode != null) {
                    val text = titleNode.text?.toString()
                    if (text == "看15秒后点击") {
                        Log.i(TAG, "看15秒后点击，15 秒后状态变成 State.RETURNING_TO_APP ")
                        driveByInnerState(State.CLICK_AD_AFTER_15_SECOND)
                    } else if (text == "点击跳转APP停留10秒") {
                        Log.i(TAG, "点击跳转APP停留10秒")
                        driveByInnerState(State.CLICK_AD_IMMEDIATE)
                    } else {
                        Log.i(TAG, "不知名文本。")
                        stopAutomation()
                    }
                } else {
                    Log.i(TAG, "找不到广告弹窗的文本。")
                    stopAutomation()
                }
            }
        )
    }

    private fun findAd(): AccessibilityNodeInfo? {
        return rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.netease.cloudmusic:id/adConsumeClick")
            .firstOrNull()
    }

    private fun handleClickADAfter15Second() {
        handler.postDelayed({
            executeWithTimeoutRetry(
                description = "15 秒时间到，点击广告领取奖励。",
                findAction = ::findAd,
                executeAction = { adButton ->
                    adButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    driveByInnerState(State.CHECK_IF_CANCEL_BUTTON, 2000L)
                },
            )
        }, 15000)
    }

    private fun handleCheckCancelButton(delay: Long) {
        executeWithTimeoutRetry(
            description = "点击广告详情页的 取消 按钮。",
            findAction = ::findCancelBtn,
            executeAction = { cancelButton ->
                cancelButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                handler.postDelayed({
                    driveByInnerState(State.RETURN_TO_APP)
                }, delay)
            },
        )
    }


    private fun handleReturnApp() {
        if (currentActivity == "com.netease.cloudmusic.activity.MainActivity") {
            handler.removeCallbacksAndMessages(null)
            driveByInnerState(State.CLICK_VIDEO_AD)
        } else {
            performGlobalAction(GLOBAL_ACTION_BACK)
            handler.postDelayed({
                handleReturnApp()
            }, 1000)
        }
    }

    private fun findCancelBtn(): AccessibilityNodeInfo? {
        return FindEle.findByText(root = rootInActiveWindow, text = "取消")
    }

    private fun handleClickADImmediateAndWait10Second() {
        executeWithTimeoutRetry(
            description = "点击跳转APP停留10秒。",
            findAction = ::findAd,
            executeAction = { adButton ->
                adButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                driveByInnerState(State.CHECK_IF_CANCEL_BUTTON_AND_WAIT, 2000L)
            },
        )
    }


    private fun driveByInnerState(newState: State, delay: Long = 1000L) {
        // 状态改变前，清除所有延时任务
        handler.removeCallbacksAndMessages(null) // 清除所有延迟任务
        currentState = newState
        Log.d(TAG, "内部状态改变 $newState，触发事件 handleStateLogic")
        handler.postDelayed({ handleStateLogic() }, delay)
    }

    private fun driveByOuterState(newState: State) {
        currentState = newState
        Log.d(TAG, "等待 TYPE_WINDOW_STATE_CHANGED 改变，触发事件")
    }

    private fun resetState() {
        currentState = State.IDLE
        hadClickFreeListenButton = false
        isRunning = false
        isScrolling = false
        handler.removeCallbacksAndMessages(null) // 清除所有延迟任务
    }


    fun findScrollableChild(container: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (container == null) return null
        if (container.isScrollable) return container

        for (i in 0 until container.childCount) {
            val child = container.getChild(i)
            val scrollable = findScrollableChild(child)
            if (scrollable != null) return scrollable
        }
        return null
    }


    fun findNodeByTextWidthoutRoot(
        root: AccessibilityNodeInfo?,
        targetText: String
    ): AccessibilityNodeInfo? {
        if (root == null) return null
        if (root.text?.toString() == targetText) return root
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            val result = findNodeByTextWidthoutRoot(child, targetText)
            if (result != null) return result
        }
        return null
    }


    // 扩展函数：给 AccessibilityNodeInfo 加方法
    fun findTextView(parent: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return (0 until parent.childCount)
            .mapNotNull { parent.getChild(it) }
            .find { it.className == "android.widget.TextView" }
    }

    private fun executeWithTimeoutRetry(
        description: String = "",
        timeoutMills: Long = 5000L,
        delayMills: Long = 500L,
        checkBeforeAction: () -> Unit = {},
        findAction: () -> AccessibilityNodeInfo?,
        executeAction: (node: AccessibilityNodeInfo?) -> Unit,
    ) {
        if (description.isNotEmpty()) {
            Log.i(TAG, description)
        }

        val startTime = System.currentTimeMillis()

        val retryRunnable = object : Runnable {
            override fun run() {
                // 专门用于检查各种弹窗
                checkBeforeAction()

                val result = findAction()
                if (result != null) {
                    handler.removeCallbacks(this)
                    executeAction(result)
                } else {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < timeoutMills) {
                        handler.postDelayed(this, delayMills)
                    } else {
                        handler.removeCallbacks(this)
                        executeAction(null)
                    }
                }
            }
        }
        handler.post(retryRunnable)
    }

}

