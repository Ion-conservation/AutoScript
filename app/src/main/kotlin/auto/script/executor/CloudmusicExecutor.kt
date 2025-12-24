package auto.script.executor

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import auto.script.A11yService.A11yServiceTool
import auto.script.common.EventTaskHandler
import auto.script.common.centerPoint
import auto.script.core.DumpManager.DumpInfo
import auto.script.core.DumpManager.DumpManager
import auto.script.core.DumpManager.FailReason
import auto.script.nodetool.NodeTool
import auto.script.shizuku.ShizukuTool
import auto.script.state.NeteaseState
import auto.script.utils.ScriptLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudmusicExecutor @Inject constructor(
    private val a11yServiceTool: A11yServiceTool,
    private val shizukuTool: ShizukuTool,
    private val nodeTool: NodeTool
) : EventTaskHandler {

    companion object {
        private const val TAG = "网易云音乐脚本"
        private const val APP_PACKAGE_NAME = "com.netease.cloudmusic"
    }

    private val handler = Handler(Looper.getMainLooper())
    private var currentState: NeteaseState = NeteaseState.WAIT_TO_LAUNCH_APP

    override fun handleAccessibilityEvent(event: AccessibilityEvent) {

        handleDialog()

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {

            handleStateLogic()
        }
    }

    fun handleDialog() {
        val popupRules = listOf(
            "com.netease.cloudmusic:id/design_bottom_sheet", // 这个是主页的弹窗
            "com.oplus.securitypermission:id/rootView", // 这个是提示想要打开小程序
            "com.netease.cloudmusic:id/positiveBtn", // 继续观看
            "com.coloros.sceneservice:id/action_bar_root", // 开启物流提醒
        )

        for (id in popupRules) {
            val nodes = a11yServiceTool.findNodeByReourceId(id)
            if (nodes != null) {
                a11yServiceTool.performActionGlobal()
                return
            }
        }

    }


    /**
     * 核心状态处理逻辑
     */
    private fun handleStateLogic() {

        when (currentState) {

            NeteaseState.WAIT_TO_LAUNCH_APP -> {
                ScriptLogger.i(TAG, "等待 APP 启动。")
            }

            NeteaseState.LAUNCHING_APP -> {
                handleClickSkipButton {
                    handleStateChange(NeteaseState.WAIT_TO_OPEN_SIDE_BAR)
                }
            }

            NeteaseState.WAIT_TO_OPEN_SIDE_BAR -> {
                ScriptLogger.i(TAG, "正在打开侧边栏。")
                handleOpenSideBar {
                    handleStateChange(NeteaseState.WAIT_TO_CLICK_FREE_BUTTON)
                }
            }

            NeteaseState.WAIT_TO_CLICK_FREE_BUTTON -> {
                ScriptLogger.i(TAG, "正在点击免费听。")
                handleClickFreeListen {
                    handleStateChange(NeteaseState.WAIT_TO_LIGHT_UP_PUZZLE)
                }
            }


            NeteaseState.WAIT_TO_LIGHT_UP_PUZZLE -> {
                ScriptLogger.i(TAG, "正在点击 看视频，点亮拼图。")
                handleLightUpPuzzle {
                    handleStateChange(NeteaseState.WAIT_TO_HANDLE_REWARD_WAY)
                }
            }

            NeteaseState.WAIT_TO_HANDLE_REWARD_WAY -> {
                ScriptLogger.i(TAG, "当前在广告页面。")
                handleRewardWay {
                    handleStateChange(NeteaseState.WAIT_TO_RETURN_APP)

                }
            }

            NeteaseState.WAIT_TO_RETURN_APP -> {
                ScriptLogger.i(TAG, "正在返回 APP。")
                handleReturnApp()
            }

            else -> {}
        }
    }

    fun startAutomation() {
        ScriptLogger.i(TAG, "startAutomation：启动脚本。")
        try {
            shizukuTool.openAppByPackageName(APP_PACKAGE_NAME)
            handleStateChange(NeteaseState.LAUNCHING_APP)
        } catch (e: IllegalStateException) {
            stopAutomation(FailReason.LAUNCHING_APP_FAILED, "启动失败: ${e.message}")
        }
    }

    /**
     * ★★★ 统一失败出口 + DumpManager ★★★
     */
    fun stopAutomation(reason: FailReason, message: String? = null) {
        ScriptLogger.i(TAG, "stopAutomation：停止脚本。原因：$reason, $message")

        handler.removeCallbacksAndMessages(null)

        // dump UI + screenshot
        val root = a11yServiceTool.getRootNode()
        val dumpInfo = DumpInfo(
            timestamp = System.currentTimeMillis(),
            state = currentState,
            reason = reason,
            message = message
        )

        DumpManager.dump(
            dumpInfo = dumpInfo,
            rootNode = root,
            takeScreenshot = { path ->
                try {
                    shizukuTool.screencap(path)
                    true
                } catch (e: Exception) {
                    false
                }
            },
            sendEmail = true
        )

        // reset
        currentState = NeteaseState.WAIT_TO_LAUNCH_APP
    }

// ---------------- 以下业务逻辑保持不变，只把 stopAutomation 改成带 reason ----------------

    fun handleClickSkipButton(callback: (() -> Unit)? = null) {

        nodeTool.findByResourceId("com.netease.cloudmusic:id/skipBtn")
            .retry(2000)
            .then { node ->
                node?.click()
            }
//        executeWithTimeoutRetry(
//            findAction = {
//                a11yServiceTool.findNodeByReourceId("com.netease.cloudmusic:id/skipBtn")
//            },
//            executeAction = { skipButton ->
//                if (skipButton != null) {
//                    ScriptLogger.i(TAG, "找到启动广告跳过按钮")
//                    skipButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                }
//                callback?.invoke()
//            }
//        )
    }

    private fun handleOpenSideBar(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                a11yServiceTool.findNodeByReourceId("com.netease.cloudmusic:id/menu_icon_container")
            },
            executeAction = { sideBarButton ->
                if (sideBarButton != null) {
                    sideBarButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    callback?.invoke()
                } else {
                    stopAutomation(FailReason.NODE_NOT_FOUND, "找不到侧边栏按钮")
                }
            }
        )
    }

    private fun handleClickFreeListen(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                val container = a11yServiceTool.findNodeByReourceId("DC_FlatList")
                a11yServiceTool.findNodeByText(container, "免费听VIP歌曲")
            },
            executeAction = { listenFreeButton ->
                if (listenFreeButton != null) {
                    val center = listenFreeButton.centerPoint()
                    center?.let { (x, y) -> shizukuTool.tap(x, y) }
                    callback?.invoke()
                } else {
                    stopAutomation(FailReason.NODE_NOT_FOUND, "找不到免费听按钮")
                }
            }
        )
    }

    private fun handleLightUpPuzzle(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                val container =
                    a11yServiceTool.findNodeByReourceId("com.netease.cloudmusic:id/rn_content")

                val watchVideoNode = a11yServiceTool.findNodeByText(container, "看视频，点亮拼图")
                if (watchVideoNode != null) return@executeWithTimeoutRetry watchVideoNode

                val finishedNode = a11yServiceTool.findNodeByText(container, "已全部点亮，明天再来")
                if (finishedNode != null) return@executeWithTimeoutRetry finishedNode

                null
            },
            executeAction = { node ->
                if (node == null) {
                    stopAutomation(FailReason.NODE_NOT_FOUND, "未找到拼图按钮")
                    return@executeWithTimeoutRetry
                }

                when (node.text?.toString()) {
                    "看视频，点亮拼图" -> {
                        val center = node.centerPoint()
                        center?.let { (x, y) -> shizukuTool.tap(x, y) }
                        callback?.invoke()
                    }

                    "已全部点亮，明天再来" -> {
                        stopAutomation(FailReason.OTHER, "今日已全部点亮")
                    }

                    else -> {
                        stopAutomation(FailReason.UNKNOWN_UI, "未知拼图文本")
                    }
                }
            }
        )
    }

    private fun handleRewardWay(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                a11yServiceTool.findNodeByReourceId("com.netease.cloudmusic:id/tv_ad_bottom_enhance_main_title")
            },
            executeAction = { titleNode ->
                if (titleNode != null) {
                    when (titleNode.text?.toString()) {
                        "看15秒后点击" -> handleWaitAndClick(callback)
                        "点击跳转APP停留10秒" -> handleClickAndWait(callback)
                        else -> stopAutomation(FailReason.UNKNOWN_UI, "未知广告文本")
                    }
                } else {
                    stopAutomation(FailReason.NODE_NOT_FOUND, "找不到广告标题")
                }
            }
        )
    }

    private fun handleWaitAndClick(callback: (() -> Unit)? = null) {
        handler.postDelayed({
            executeWithTimeoutRetry(
                findAction = {
                    a11yServiceTool.findNodeByReourceId("com.netease.cloudmusic:id/adConsumeClick")
                },
                executeAction = { adButton ->
                    if (adButton != null) {
                        adButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        callback?.invoke()
                    } else {
                        stopAutomation(FailReason.NODE_NOT_FOUND, "广告点击按钮不存在")
                    }
                }
            )
        }, 15000L)
    }

    private fun handleClickAndWait(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                a11yServiceTool.findNodeByReourceId("com.netease.cloudmusic:id/adConsumeClick")
            },
            executeAction = { adButton ->
                if (adButton != null) {
                    adButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    handler.postDelayed({ callback?.invoke() }, 10000L)
                } else {
                    stopAutomation(FailReason.NODE_NOT_FOUND, "广告跳转按钮不存在")
                }
            }
        )
    }

    private fun handleReturnApp(callback: (() -> Unit)? = null) {
        handler.removeCallbacksAndMessages(null)

        // ------------------ 处理美团按钮导致全局返回失效问题 ------------------

        val meituanButton = a11yServiceTool.findNodeByReourceId("com.sankuai.meituan:id/btn_left")
        if (meituanButton != null) {
            meituanButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            handler.postDelayed({ handleReturnApp(callback) }, 1000L)
        }

        val container = a11yServiceTool.findNodeByReourceId("com.netease.cloudmusic:id/rn_content")

        ScriptLogger.i(TAG, "找到 container")

        val watchVideoNode = a11yServiceTool.findNodeByText(container, "看视频，点亮拼图")
        if (watchVideoNode != null) {
            ScriptLogger.i(TAG, "找到 看视频，点亮拼图")
            currentState = NeteaseState.WAIT_TO_LIGHT_UP_PUZZLE
            handleLightUpPuzzle {
                handleStateChange(NeteaseState.WAIT_TO_HANDLE_REWARD_WAY)
            }
            return
        }

        val finishedNode = a11yServiceTool.findNodeByText(container, "已全部点亮，明天再来")
        if (finishedNode != null) {
            ScriptLogger.i(TAG, "找到 已全部点亮，明天再来")
            handleStateChange(NeteaseState.WAIT_TO_LAUNCH_APP)
            stopAutomation(reason = FailReason.OTHER, "执行完成。")
            return
        }
        ScriptLogger.i(TAG, "继续返回 APP")
        a11yServiceTool.performActionGlobal()
        handler.postDelayed({ handleReturnApp(callback) }, 1000L)

    }

    private fun handleStateChange(newState: NeteaseState) {
        handler.removeCallbacksAndMessages(null)
        currentState = newState
        handleStateLogic()
    }

    private fun executeWithTimeoutRetry(
        timeoutMills: Long = 2000L,
        delayMills: Long = 500L,
        checkBeforeAction: () -> Unit = {},
        findAction: () -> AccessibilityNodeInfo?,
        executeAction: (node: AccessibilityNodeInfo?) -> Unit,
    ) {
        val startTime = System.currentTimeMillis()

        val retryRunnable = object : Runnable {
            override fun run() {
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
