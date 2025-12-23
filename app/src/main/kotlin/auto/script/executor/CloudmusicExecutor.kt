package auto.script.executor

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import auto.script.A11yService.A11yServiceTool
import auto.script.common.EventTaskHandler
import auto.script.common.centerPoint
import auto.script.shizuku.ShizukuTool
import auto.script.utils.ScriptLogger
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CloudmusicExecutor @Inject constructor(
    private val a11yServiceTool: A11yServiceTool,
    private val shizukuTool: ShizukuTool
) : EventTaskHandler {

    companion object {
        private const val TAG = "网易云音乐脚本"
        private const val APP_PACKAGE_NAME = "com.netease.cloudmusic"
        private val handler = Handler(Looper.getMainLooper())

        // 使用一个简单的状态机来管理自动化流程
        private var currentState = State.WAIT_TO_LAUNCH_APP
        private var currentClassName = ""
        private var currentPackageName = ""

        // 自动化流程的状态枚举
        private enum class State {
            WAIT_TO_LAUNCH_APP, // 空闲状态，等待应用启动
            LAUNCHING_APP, // 正在启动 APP
            WAIT_TO_OPEN_SIDE_BAR, // 通过打开抽屉来进入免费听
            WAIT_TO_CLICK_FREE_BUTTON,   // 点击 “免费听” 按钮
            WAIT_TO_LIGHT_UP_PUZZLE, // 找到 ”看视频，点亮拼图“ 按钮，并点击
            WAIT_TO_HANDLE_REWARD_WAY, // 点击广告，进入详情页
            WAIT_TO_RETURN_APP,
        }

        private var isLaunchingAppLock = false
        private var isAdHandled = false
        private var isWaitToOpenSideBar = false
        private var isWaitToClickFreeButtonLock = false
        private var isWaitToLightUpPuzzleLock = false
        private var isWaitToHandleRewardWayLock = false
        private var isWaitToReturnAppLock = false

    }

    override fun handleAccessibilityEvent(event: AccessibilityEvent) { // 处理事件

//        if (checkAndHandlePopup()) {
//            return // 弹窗已处理，本次事件不再继续执行状态机
//        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            currentPackageName = event.packageName.toString()
            currentClassName = event.className.toString()

            ScriptLogger.i(
                TAG,
                "${event.eventType}: $currentPackageName, $currentClassName, $currentState"
            )

            handleStateLogic(currentPackageName, currentClassName)
        }
    }


    /**
     * 核心状态处理逻辑
     */
    private fun handleStateLogic(packageName: String = "", className: String = "") {

        when (currentState) {
            /**
             *  WAIT_TO_LAUNCH_APP 等待用户点击按钮启动程序
             *  APP 启动时将状态更改成 LAUNCHING_APP
             * */
            State.WAIT_TO_LAUNCH_APP -> {
                ScriptLogger.i(TAG, "等待 APP 启动。")
            }

            /**
             *  LAUNCHING_APP APP 正在启动
             *  延迟 1.5 秒后将状态更改为 WAIT_TO_OPEN_SIDE_BAR
             *  原因：应对可能出现的启动广告，如果出现广告，则清除延时执行，点击跳过广告后，重新将状态更改为 WAIT_TO_OPEN_SIDE_BAR
             * */
            State.LAUNCHING_APP -> {
                ScriptLogger.i(TAG, "正在启动 APP。")

                if (!isAdHandled &&
                    packageName == APP_PACKAGE_NAME &&
                    className == "android.widget.RelativeLayout"
                ) {
                    isAdHandled = true  // ★ 关键：只处理一次广告
                    ScriptLogger.i(TAG, "发现 APP 启动广告")

                    handler.removeCallbacksAndMessages(null)

                    handleClickSkipButton {
                        driveDelayed(State.WAIT_TO_OPEN_SIDE_BAR, 1500L)
                    }

                    return
                }

                if (!isLaunchingAppLock) {
                    isLaunchingAppLock = true
                    driveDelayed(State.WAIT_TO_OPEN_SIDE_BAR, 1500L)
                }
            }


            /**
             *  WAIT_TO_OPEN_SIDE_BAR 等待点击侧边栏
             *  状态变更为 WAIT_TO_CLICK_FREE_BUTTON
             * */
            State.WAIT_TO_OPEN_SIDE_BAR -> {
                if (!isWaitToOpenSideBar) {
                    isWaitToOpenSideBar = true
                    ScriptLogger.i(TAG, "正在打开侧边栏。")
                    handleOpenSideBar {
                        driveByOuterState(State.WAIT_TO_CLICK_FREE_BUTTON)
                        isWaitToOpenSideBar = false
                    }
                }

            }


            /**
             *  WAIT_TO_CLICK_FREE_BUTTON 等待点击免费听
             *  状态变更为 WAIT_TO_LIGHT_UP_PUZZLE
             * */
            State.WAIT_TO_CLICK_FREE_BUTTON -> {
                if (!isWaitToClickFreeButtonLock) {
                    if (packageName == APP_PACKAGE_NAME &&
                        className == "androidx.drawerlayout.widget.DrawerLayout"
                    ) {
                        isWaitToClickFreeButtonLock = true

                        ScriptLogger.i(TAG, "正在点击免费听。")
                        handleClickFreeListen {
                            driveByOuterState(State.WAIT_TO_LIGHT_UP_PUZZLE)
                        }
                    }
                }

            }


            /**
             *  WAIT_TO_LIGHT_UP_PUZZLE 等待点击看视频按钮
             *  状态变更为 WAIT_TO_HANDLE_REWARD_WAY
             * */
            State.WAIT_TO_LIGHT_UP_PUZZLE -> {
                if (!isWaitToLightUpPuzzleLock) {
                    if (packageName == APP_PACKAGE_NAME &&
                        className == "com.netease.cloudmusic.activity.MainActivity"
                    ) {
                        isWaitToLightUpPuzzleLock = true

                        ScriptLogger.i(TAG, "正在点击 看视频，点亮拼图。")
                        handleLightUpPuzzle {
                            driveByOuterState(State.WAIT_TO_HANDLE_REWARD_WAY)
                        }
                    }
                }

                if (packageName == APP_PACKAGE_NAME &&
                    className == "com.afollestad.materialdialogs.d"
                ) {
                    ScriptLogger.i(TAG, "多了一个广告弹窗，执行一次返回。")
                    a11yServiceTool.performActionGlobal()
                }

            }

            /**
             *  WAIT_TO_HANDLE_REWARD_WAY 等待点击看视频按钮
             *  统一处理两种情况：看15秒后点击、点击跳转APP停留10秒
             * */

            State.WAIT_TO_HANDLE_REWARD_WAY -> {
                if (!isWaitToHandleRewardWayLock) {
                    if (packageName == APP_PACKAGE_NAME &&
                        className == "com.netease.cloudmusic.module.ad.motivation.commonui.AdMotivationVideoActivity"
                    ) {

                        isWaitToHandleRewardWayLock = true
                        isWaitToLightUpPuzzleLock = false

                        ScriptLogger.i(TAG, "当前在广告页面。")
                        handleRewardWay {
                            // 此处由内部驱动，因为 A11yService 无法自动触发返回
                            driveByInnerState(State.WAIT_TO_RETURN_APP)
                            isWaitToHandleRewardWayLock = false
                        }
                    }
                }

            }

            /**
             *  WAIT_TO_RETURN_APP 看完广告返回 APP
             *  状态变更为 WAIT_TO_LIGHT_UP_PUZZLE
             * */

            State.WAIT_TO_RETURN_APP -> {
                if (!isWaitToReturnAppLock) {
                    isWaitToReturnAppLock = true
                    ScriptLogger.i(TAG, "正在返回 APP。")
                    handleReturnApp {
                        driveByOuterState(State.WAIT_TO_LIGHT_UP_PUZZLE)
                        isWaitToReturnAppLock = false
                    }
                }

            }

//            className == "com.netease.cloudmusic.ui.FeatureDialog" -> {
//                ScriptLogger.i(TAG, "遇到升级的弹窗。")
//                a11yService?.performActionGlobal()
//            }
//
//            className == "i83.a" -> {
//                ScriptLogger.i(TAG, "遇到升级完成之后的弹窗。")
//                a11yService?.performActionGlobal()
//            }


//            else -> {
            // 这里可能用于处理弹窗等情况会比较合适
            // TYPE_WINDOW_STATE_CHANGED className: com.netease.cloudmusic.ui.FeatureDialog 升级的弹窗
            // TYPE_WINDOW_STATE_CHANGED className: i83.a 升级完成之后的弹窗
//            }
        }
    }

    fun startAutomation() {
        ScriptLogger.i(TAG, "startAutomation：启动脚本。")
        try {
            shizukuTool.openAppByPackageName(APP_PACKAGE_NAME)

            driveImmediate(State.LAUNCHING_APP)
        } catch (e: IllegalStateException) {
            ScriptLogger.e(TAG, "启动失败: ${e.message}")
        }
    }

    fun stopAutomation() {
        ScriptLogger.i(TAG, "stopAutomation：停止脚本。")
        handler.removeCallbacksAndMessages(null)
        currentState = State.WAIT_TO_LAUNCH_APP
        currentClassName = ""

        isLaunchingAppLock = false
        isAdHandled = false
        isWaitToOpenSideBar = false
        isWaitToClickFreeButtonLock = false
        isWaitToLightUpPuzzleLock = false
        isWaitToReturnAppLock = false
    }

    fun handleClickSkipButton(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                a11yServiceTool.findNodeByReourceId("com.netease.cloudmusic:id/skipBtn")
            },
            executeAction = { skipButton ->
                if (skipButton != null) {
                    ScriptLogger.i(
                        TAG,
                        "handleClickSkipButton：找到 App 启动广告 '跳过' 按钮，尝试点击。"
                    )
                    skipButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                } else {
                    ScriptLogger.i(
                        TAG,
                        "handleClickSkipButton：找不到 App 启动广告 '跳过' 按钮，等待下一个状态。"
                    )
                }
                callback?.invoke()
            },
        )
    }

    private fun handleOpenSideBar(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                val sideBarResourceId = "com.netease.cloudmusic:id/menu_icon_container"
                a11yServiceTool.findNodeByReourceId(sideBarResourceId)
            },
            executeAction = { sideBarButton ->
                if (sideBarButton != null) {
                    ScriptLogger.i(TAG, "handleOpenSideBar：找到侧边栏菜单按钮，尝试点击点击。")
                    sideBarButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)

                    callback?.invoke()
                } else {
                    ScriptLogger.i(TAG, "handleOpenSideBar：找不到侧边栏菜单按钮，退出程序。")
                    stopAutomation()
                }
            }
        )
    }

    private fun handleClickFreeListen(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                val dcResourceId = "DC_FlatList"
                val container = a11yServiceTool.findNodeByReourceId(dcResourceId)
                a11yServiceTool.findNodeByText(container, "免费听VIP歌曲")
            },
            executeAction = { listenFreeButton ->
                if (listenFreeButton != null) {
                    ScriptLogger.i(TAG, "handleClickFreeListen：找到免费听VIP歌曲按钮，尝试点击。")
                    val center = listenFreeButton.centerPoint()
                    center?.let { (x, y) ->
                        shizukuTool.tap(x, y)
                    }
                    callback?.invoke()

                } else {
                    ScriptLogger.i(TAG, "handleClickFreeListen：找不到免费听VIP歌曲按钮，退出程序。")
                    stopAutomation()
                }
            }
        )
    }


    private fun handleLightUpPuzzle(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                val container =
                    a11yServiceTool.findNodeByReourceId("com.netease.cloudmusic:id/rn_content")

                // ① 查找“看视频，点亮拼图”
                val watchVideoNode = a11yServiceTool.findNodeByText(container, "看视频，点亮拼图")
                if (watchVideoNode != null) {
                    return@executeWithTimeoutRetry watchVideoNode
                }

                // ② 查找“已全部点亮，明天再来”
                val finishedNode = a11yServiceTool.findNodeByText(container, "已全部点亮，明天再来")
                if (finishedNode != null) {
                    return@executeWithTimeoutRetry finishedNode
                }

                null
            },
            executeAction = { node ->
                if (node == null) {
                    ScriptLogger.i(TAG, "handleLightUpPuzzle：未找到拼图按钮，退出程序。")
                    stopAutomation()
                    return@executeWithTimeoutRetry
                }

                val text = node.text?.toString() ?: ""

                when (text) {
                    "看视频，点亮拼图" -> {
                        ScriptLogger.i(TAG, "handleLightUpPuzzle：找到“看视频，点亮拼图”，尝试点击。")
                        val center = node.centerPoint()
                        center?.let { (x, y) ->
                            shizukuTool.tap(x, y)
                        }
                        callback?.invoke()
                    }

                    "已全部点亮，明天再来" -> {
                        ScriptLogger.i(TAG, "handleLightUpPuzzle：今日已全部点亮，脚本结束。")
                        stopAutomation()
                    }

                    else -> {
                        ScriptLogger.i(TAG, "handleLightUpPuzzle：未知文本：$text，退出程序。")
                        stopAutomation()
                    }
                }
            }
        )
    }


    private fun handleRewardWay(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                val adTitleResourceId = "com.netease.cloudmusic:id/tv_ad_bottom_enhance_main_title"
                a11yServiceTool.findNodeByReourceId(adTitleResourceId)
            },
            executeAction = { titleNode ->
                if (titleNode != null) {
                    val text = titleNode.text?.toString()
                    when (text) {
                        "看15秒后点击" -> {
                            ScriptLogger.i(TAG, "handleRewardWay：看15秒后点击")
                            handleWaitAndClick(callback)
                        }

                        "点击跳转APP停留10秒" -> {
                            ScriptLogger.i(TAG, "handleRewardWay：点击跳转APP停留10秒")
                            handleClickAndWait(callback)
                        }

                        else -> {
                            ScriptLogger.i(TAG, "handleRewardWay：不知名文本，退出程序。")
                            stopAutomation()
                        }
                    }
                } else {
                    ScriptLogger.i(TAG, "handleRewardWay：找不到广告弹窗的文本，退出程序。")
                    stopAutomation()
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
                        ScriptLogger.i(TAG, "handleWaitAndClick：尝试点击广告。")
                        adButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        callback?.invoke()
                    } else {
                        ScriptLogger.i(TAG, "handleWaitAndClick：找不到广告，退出程序。")
                        stopAutomation()
                    }
                },
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
                    ScriptLogger.i(TAG, "handleClickAndWait：找到广告，尝试点击。")
                    adButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    handler.postDelayed({
                        callback?.invoke()
                    }, 10000L)
                } else {
                    ScriptLogger.i(TAG, "handleClickAndWait：找不到广告，退出程序。")
                    stopAutomation()
                }


            },
        )
    }


    private fun handleReturnApp(callback: (() -> Unit)? = null) {
        handler.removeCallbacksAndMessages(null)
        if (currentPackageName == "com.android.launcher") {
            ScriptLogger.i(TAG, "回到了桌面。")
//            a11yServiceTool?.backToApp(APP_PACKAGE_NAME)
            handler.postDelayed({
                handleReturnApp(callback)
            }, 1000L)
        }
        if (currentClassName == "com.afollestad.materialdialogs.d" || currentClassName == "com.netease.cloudmusic.module.ad.motivation.commonui.AdMotivationVideoActivity") {
            ScriptLogger.i(TAG, "最后一次返回")
            a11yServiceTool.performActionGlobal()
            callback?.invoke()
        } else {
            ScriptLogger.i(TAG, "还需执行返回")
            a11yServiceTool.performActionGlobal()
            handler.postDelayed({
                handleReturnApp(callback)
            }, 1000L)
        }
    }


    private fun driveImmediate(newState: State) {
        handler.removeCallbacksAndMessages(null)
        currentState = newState
        handleStateLogic()
    }

    private fun driveDelayed(newState: State, delay: Long) {
        handler.removeCallbacksAndMessages(null)

        handler.postDelayed({
            currentState = newState
            handleStateLogic()
        }, delay)
    }


    private fun driveByInnerState(newState: State, delay: Long = 1000L) {
        handler.removeCallbacksAndMessages(null) // 清除所有延迟任务
        handler.postDelayed({
            ScriptLogger.d(TAG, "driveByInnerState：newState：$newState")
            currentState = newState
            handleStateLogic()
        }, delay)
    }

    private fun driveByOuterState(newState: State, delay: Long = 0L) {
        ScriptLogger.d(TAG, "driveByOuterState：newState：$newState")
        handler.removeCallbacksAndMessages(null)

        val runnable = Runnable { currentState = newState }

        if (delay > 0) handler.postDelayed(runnable, delay)
        else runnable.run()
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

    private fun checkAndHandlePopup(): Boolean {
//        val root = a11yServiceTool.getRootNode() ?: return false

        // 1. 常见关闭按钮
//        val closeTexts = listOf("关闭", "我知道了", "知道了", "取消", "不再提示", "以后再说")
//        for (text in closeTexts) {
//            val node = a11yServiceTool.findNodeByText(root, text)
//            if (node != null) {
//                ScriptLogger.i(TAG, "检测到弹窗：$text，尝试点击关闭。")
//                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                return true
//            }
//        }

        // 2. 常见 resource-id（网易云的弹窗 id）
        val popupIds = listOf(
//            "com.netease.cloudmusic:id/close",
//            "com.netease.cloudmusic:id/iv_close",
//            "com.netease.cloudmusic:id/btn_close",
//            "com.netease.cloudmusic.ui.FeatureDialog", // classname
//            "i83.a", // classname
            "com.netease.cloudmusic:id/design_bottom_sheet"
        )
        for (id in popupIds) {
            val node = a11yServiceTool.findNodeByReourceId(id)
            if (node != null) {
                ScriptLogger.i(TAG, "检测到弹窗 id：$id，尝试点击关闭。")
//                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                a11yServiceTool.performActionGlobal()
                return true
            }
        }

        return false
    }


}

