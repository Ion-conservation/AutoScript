package auto.script.executor

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import auto.script.A11yService.A11yServiceTool
import auto.script.common.EventTaskHandler
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

    // 关键：防止上一轮逻辑没跑完，下一轮心跳又进来了
    private var isBusy = false

    // -------------------------
    // 心跳机制
    // -------------------------
    private var currentInterval = 1000L
    private var heartbeatPaused = false

    object AppStatusMonitor {
        var currentPackage: String? = null
    }

    private val heartbeatTask = object : Runnable {
        override fun run() {
            if (heartbeatPaused) return // 如果被显式暂停，停止循环

            // 2. 正常逻辑执行
            if (!isBusy) {
                val currentPkg = AppStatusMonitor.currentPackage

                if (currentPkg == APP_PACKAGE_NAME) {
                    handleStateLogic()
                } else if (currentPkg != null) {
                    // 只有明确拿到了非目标包名，才执行返回逻辑
                    handleReturnAppLogic()
                }
            }

            // 3. 动态调整频率
            val dynamicInterval = when (currentState) {
                NeteaseState.WAIT_TO_HANDLE_REWARD_WAY -> 5000L // 广告页慢一点
                NeteaseState.LAUNCHING_APP -> 500L           // 启动页快一点
                else -> 1000L
            }

            handler.postDelayed(this, dynamicInterval)
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
        val root = a11yServiceTool.getRootNode()
        val dumpInfo = DumpInfo(System.currentTimeMillis(), currentState, reason, message)
        DumpManager.dump(dumpInfo, root, { path ->
            try {
                shizukuTool.screencap(path); true
            } catch (e: Exception) {
                false
            }
        }, true)

        currentState = NeteaseState.WAIT_TO_LAUNCH_APP
    }

    // -------------------------
    // 核心状态机（由心跳驱动）
    // -------------------------
    private fun handleStateLogic() {

        // 每次执行前先处理弹窗
        handleDialog()

        when (currentState) {
            NeteaseState.LAUNCHING_APP -> {
                checkAndClick("com.netease.cloudmusic:id/skipBtn", "跳过广告") {
                    currentState = NeteaseState.WAIT_TO_OPEN_SIDE_BAR
                }
            }

            NeteaseState.WAIT_TO_OPEN_SIDE_BAR -> {
                checkAndClick("com.netease.cloudmusic:id/menu_icon_container", "侧边栏") {
                    currentState = NeteaseState.WAIT_TO_CLICK_FREE_BUTTON
                }
            }

            NeteaseState.WAIT_TO_CLICK_FREE_BUTTON -> {
                // 用文本查找
                checkAndClickByText("免费听VIP歌曲", "免费听按钮") {
                    currentState = NeteaseState.WAIT_TO_LIGHT_UP_PUZZLE
                }
            }

            NeteaseState.WAIT_TO_LIGHT_UP_PUZZLE -> {
                handlePuzzleState()
            }

            NeteaseState.WAIT_TO_HANDLE_REWARD_WAY -> {
                handleAdRewardLogic()
            }

            else -> { /* 其他状态或等待中 */
            }
        }
    }

    // -------------------------
    // 业务逻辑封装（非阻塞式）
    // -------------------------

    /**
     * 封装 ID 查找点击，不使用内部 retry，依靠心跳自然重试
     */
    private fun checkAndClick(resId: String, label: String, onSuccess: () -> Unit) {
        isBusy = true
        nodeTool.findByResourceId(resId).then { node ->
            if (node != null) {
                ScriptLogger.i(TAG, "找到 $label，执行点击")
                node.click()
                onSuccess()
            }
            isBusy = false // 无论找没找到，干完活就把锁释放
        }.start()
    }

    private fun checkAndClickByText(text: String, label: String, onSuccess: () -> Unit) {
        isBusy = true
        nodeTool.findByText(text).then { node ->
            if (node != null) {
                ScriptLogger.i(TAG, "找到 $label，执行点击")
                node.click()
                onSuccess()
            }
            isBusy = false
        }.start()
    }

    /**
     * 处理拼图页面的特殊逻辑
     */
    private fun handlePuzzleState() {
        isBusy = true
        nodeTool.findByText("看视频，点亮拼图").then { node ->
            if (node != null) {
                node.click()
                currentState = NeteaseState.WAIT_TO_HANDLE_REWARD_WAY
                isBusy = false
            } else {
                // 没找到点亮，找找是不是完成了
                nodeTool.findByText("已全部点亮，明天再来").then { finishedNode ->
                    if (finishedNode != null) {
                        stopAutomation(FailReason.OTHER, "拼图任务已全部完成")
                    }
                    isBusy = false
                }.start()
            }
        }.start()
    }

    /**
     * 广告奖励页面的复杂逻辑处理
     */
    private fun handleAdRewardLogic() {
        isBusy = true
        val resId = "com.netease.cloudmusic:id/tv_ad_bottom_enhance_main_title"
        nodeTool.findByResourceId(resId).then { node ->
            val adText = node?.text ?: ""
            ScriptLogger.i(TAG, "检测到广告类型: $adText")

            when {
                adText.contains("看15秒") -> {
                    // 这里由于需要等待 15 秒，我们暂时不释放 isBusy，
                    // 相当于“掐断”心跳，直到倒计时结束
                    handler.postDelayed({
                        clickAdButton()
                    }, 16000L)
                }

                adText.contains("跳转APP") -> {
                    node?.click()
                    handler.postDelayed({
                        currentState = NeteaseState.WAIT_TO_RETURN_APP
                        isBusy = false
                    }, 11000L)
                }

                else -> {
                    isBusy = false // 没识别到，让心跳下一秒再试
                }
            }
        }.start()
    }

    private fun clickAdButton() {
        val btnId = "com.netease.cloudmusic:id/adConsumeClick"
        nodeTool.findByResourceId(btnId).then { node ->
            node?.click()
            currentState = NeteaseState.WAIT_TO_RETURN_APP
            isBusy = false
        }.start()
    }

    // -------------------------
    // 原有工具方法保持
    // -------------------------

    override fun handleAccessibilityEvent(event: AccessibilityEvent) {
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

        val currentPkg = shizukuTool.getCurrentPackageName()

        when {
            // 如果在桌面，尝试通过 Shizuku 唤起
            currentPkg == "com.android.launcher" || currentPkg?.contains("launcher") == true -> {
                ScriptLogger.i(TAG, "回到了桌面，尝试重新唤起...")
                shizukuTool.openAppByPackageName(APP_PACKAGE_NAME)
            }
            // 如果在其他干扰 App（如美团），尝试点击其自带的关闭/返回
            currentPkg == "com.sankuai.meituan" -> {
                // 这里的 checkAndClick 内部要设 isBusy = true
                checkAndClick("com.sankuai.meituan:id/btn_left", "美团返回") { }
            }
            // 通用情况：执行全局返回动作
            else -> {
                ScriptLogger.i(TAG, "执行全局返回...")
                a11yServiceTool.performActionGlobal() // ACTION_BACK
            }
        }
    }
}