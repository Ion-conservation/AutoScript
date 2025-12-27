package auto.script.feature.netease

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import auto.script.A11yService.A11yServiceTool
import auto.script.common.EventTaskHandler
import auto.script.common.getCenterPoint
import auto.script.core.DumpManager.FailReason
import auto.script.nodetool.NodeTool
import auto.script.shizuku.ShizukuTool
import auto.script.state.NeteaseState
import auto.script.utils.ScriptLogger
import auto.script.utils.ScriptUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NeteaseExecutor @Inject constructor(
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

    private var currentPackage: String? = ""

    // 关键：防止上一轮逻辑没跑完，下一轮心跳又进来了
    private var isBusy = false


    object AppStatusMonitor {
        var currentPackage: String? = null
    }

    private val heartbeatTask = object : Runnable {
        override fun run() {

            if (!isBusy) {
                val currentPkg = AppStatusMonitor.currentPackage

                if (currentPkg == APP_PACKAGE_NAME) {
                    ScriptLogger.i(TAG, "心跳")
                    handleStateLogic()
                } else if (currentPkg != null) {
                    // 只有明确拿到了非目标包名，才执行返回逻辑
                    handleReturnAppLogic()
                }
            }

            handler.postDelayed(this, 1000L)
        }
    }

    /**
     * 启动自动化（入口）
     */
    fun startAutomation() {
        ScriptLogger.i(TAG, "startAutomation：启动脚本。")
        try {
            shizukuTool.openAppByPackageName(APP_PACKAGE_NAME)

            // 1. 初始化状态
            currentState = NeteaseState.LAUNCHING_APP
            isBusy = false

            // 2. 开启心跳（先移除旧的，防止双倍心跳）
            handler.removeCallbacks(heartbeatTask)
            handler.postDelayed(heartbeatTask, 1000L)

        } catch (e: IllegalStateException) {
            stopAutomation(FailReason.LAUNCHING_APP_FAILED, "启动失败: ${e.message}")
        }
    }

    /**
     * 停止自动化
     */
    fun stopAutomation(reason: FailReason, message: String? = null) {
        ScriptLogger.i(TAG, "stopAutomation：停止脚本。原因：$reason, $message")

        // 停止心跳
        handler.removeCallbacks(heartbeatTask)
        isBusy = false

        // Dump 逻辑（保持你原有的）
//        val root = a11yServiceTool.getRootNode()
//        val dumpInfo = DumpInfo(System.currentTimeMillis(), currentState, reason, message)
//        DumpManager.dump(dumpInfo, root, { path ->
//            try {
//                shizukuTool.screencap(path); true
//            } catch (e: Exception) {
//                false
//            }
//        }, true)

        currentState = NeteaseState.WAIT_TO_LAUNCH_APP
    }


    private fun handleStateLogic() {


        // 每次执行前先处理弹窗
        handleDialog()

        when (currentState) {
            NeteaseState.LAUNCHING_APP -> handleLaunchingApp {
                isBusy = false
                currentState = NeteaseState.WAIT_TO_OPEN_SIDE_BAR
            }

            NeteaseState.WAIT_TO_OPEN_SIDE_BAR -> handleOpenSideBar {
                isBusy = false
                currentState = NeteaseState.WAIT_TO_CLICK_FREE_BUTTON
            }

            NeteaseState.WAIT_TO_CLICK_FREE_BUTTON -> handleClickFreeButton {
                isBusy = false
                currentState = NeteaseState.WAIT_TO_LIGHT_UP_PUZZLE
            }

            NeteaseState.WAIT_TO_LIGHT_UP_PUZZLE -> handlePuzzleState {
                currentState = NeteaseState.WAIT_TO_HANDLE_REWARD_WAY
                isBusy = false
            }

            NeteaseState.WAIT_TO_HANDLE_REWARD_WAY -> handleAdRewardLogic {
                currentState = NeteaseState.WAIT_TO_RETURN_APP
                isBusy = false
            }

            NeteaseState.WAIT_TO_RETURN_APP -> handleReturnAppLogic()

            else -> {}

        }
    }

    // -------------------------
    // 业务逻辑封装（非阻塞式）
    // -------------------------

    fun handleLaunchingApp(onSuccess: () -> Unit) {
        isBusy = true
        ScriptLogger.i(TAG, "正在启动应用，同时检测启动广告。")
        val resourceId = "com.netease.cloudmusic:id/skipBtn"
        nodeTool.findByResourceId(resourceId)
            .retry(timeout = 2000L)
            .then { node ->
                ScriptLogger.i(TAG, "检测到广告，点击跳过。")
                node.click()
                onSuccess()
            }.fail {
                onSuccess()
            }.start()
    }

    fun handleOpenSideBar(onSuccess: () -> Unit) {
        isBusy = true
        ScriptLogger.i(TAG, "正在打开侧边栏。")
        val resourceId = "com.netease.cloudmusic:id/menu_icon_container"
        nodeTool.findByResourceId(resourceId)
            .then { node ->
                node.click()
                onSuccess()
            }.fail {
                stopAutomation(FailReason.NODE_NOT_FOUND, "找不到侧边栏按钮")
            }.start()
    }

    // WIP
    fun handleClickFreeButton(onSuccess: () -> Unit) {
        isBusy = true
        ScriptLogger.i(TAG, "点击 免费听VIP歌曲。")
        val text = "免费听VIP歌曲"
        nodeTool.findByText(text)
            .then { node ->
                val center = node.getCenterPoint()
                center?.let { (x, y) ->
                    shizukuTool.tap(x, y)
                }
                onSuccess()

            }.fail {
                stopAutomation(FailReason.NODE_NOT_FOUND, "找不到侧边栏按钮")
            }.start()
    }


    /**
     * 处理拼图页面的特殊逻辑
     */
    private fun handlePuzzleState(onSuccess: () -> Unit) {
        isBusy = true
        nodeTool.findByText("看视频，点亮拼图")
            .then { node ->
                val center = node.getCenterPoint()
                center?.let { (x, y) ->
                    shizukuTool.tap(x, y)
                }
                onSuccess()
            }
            .fail {
                nodeTool.findByText("已全部点亮，明天再来").then { finishedNode ->
                    stopAutomation(FailReason.OTHER, "拼图任务已全部完成")
                    isBusy = false
                }.start()
            }.start()
    }

    /**
     * 广告奖励页面的复杂逻辑处理
     */
    private fun handleAdRewardLogic(onSuccess: () -> Unit) {
        isBusy = true
        val resId = "com.netease.cloudmusic:id/tv_ad_bottom_enhance_main_title"
        nodeTool.findByResourceId(resId).then { node ->
            val adText = node.text ?: ""
            ScriptLogger.i(TAG, "检测到广告类型: $adText")
            val center = node.getCenterPoint()
            when (adText) {
                "看15秒后点击" -> {
                    handler.postDelayed({
                        center?.let { (x, y) ->
                            shizukuTool.tap(x, y)
                        }
                        onSuccess()

                    }, 16000L)
                }

                "点击跳转APP停留10秒" -> {
                    center?.let { (x, y) ->
                        shizukuTool.tap(x, y)
                    }
                    handler.postDelayed({

                        onSuccess()
                    }, 11000L)
                }
            }
        }.start()
    }


    override fun handleAccessibilityEvent(event: AccessibilityEvent) {
        currentPackage = event.packageName?.toString()
        // 仅处理全局弹窗，不驱动状态机
        val pkg = event.packageName?.toString()
        if (!pkg.isNullOrBlank()) {
            AppStatusMonitor.currentPackage = pkg
        }

        handleDialog()
    }

    fun handleDialog() {
        val popupRules = listOf(
            "com.netease.cloudmusic:id/design_bottom_sheet",
            "com.oplus.securitypermission:id/rootView",
            "com.netease.cloudmusic:id/positiveBtn"
        )
        for (id in popupRules) {
            val nodes = a11yServiceTool.findNodeByReourceId(id)
            if (nodes != null) {
                ScriptLogger.i(TAG, "检测到干扰弹窗: $id，尝试关闭")
                a11yServiceTool.performActionGlobal() // 或其他关闭逻辑
                return
            }
        }
    }

    private fun handleReturnAppLogic() {
        // 停止所有待执行的任务，防止逻辑重叠
        handler.removeCallbacksAndMessages(null)

        // ------------------ 情况一：返回了桌面，需要重新唤起 ------------------
        if (currentPackage == "com.android.launcher") {
            ScriptLogger.i(TAG, "回到了桌面，尝试重新唤起...")
            ScriptUtils.openApp(APP_PACKAGE_NAME)
            handler.postDelayed({
                handleReturnAppLogic()
            }, 2000L)
            return
        }

        // ------------------ 情况二：找到了 "看视频，点亮拼图"，继续执行点亮操作 ------------------
        val node = a11yServiceTool.findNodeByText(a11yServiceTool.getRootNode(), "看视频，点亮拼图")
        if (node != null) {
            val center = node.getCenterPoint()
            center?.let { (x, y) ->
                shizukuTool.tap(x, y)
            }
            currentState = NeteaseState.WAIT_TO_HANDLE_REWARD_WAY
            isBusy = false
            return
        }

        // ------------------ 情况三：找到了 "已全部点亮，明天再来"，停止脚本 ------------------
        if (a11yServiceTool.findNodeByText(
                a11yServiceTool.getRootNode(),
                "已全部点亮，明天再来"
            ) != null
        ) {
            stopAutomation(reason = FailReason.OTHER, message = "已全部点亮，明天再来")
            return
        }

        // ------------------ 其他：执行返回操作 ------------------
        a11yServiceTool.performActionGlobal()
        handler.postDelayed({
            handleReturnAppLogic()
        }, 1200L)
    }
}