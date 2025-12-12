package auto.script.executor

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import auto.script.common.EventTaskHandler
import auto.script.common.centerPoint
import auto.script.service.AutomationService
import auto.script.shizuku.IMyShizukuService
import auto.script.utils.ScriptLogger
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CloudmusicExecutor @Inject constructor() : EventTaskHandler {

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
        private var isWaitToOpenSideBar = false
        private var isWaitToClickFreeButtonLock = false
        private var isWaitToLightUpPuzzleLock = false
        private var isWaitToHandleRewardWayLock = false
        private var isWaitToReturnAppLock = false

    }

    // ------------------ a11yService start ------------------
    private var a11yService: AutomationService? = null
    fun attachA11yService(service: AutomationService) {
        this.a11yService = service
    }

    fun detachA11yService() {
        this.a11yService = null
    }
    // ------------------ a11yService end ------------------


    // ------------------ shizukuService start ------------------
    var shizukuService: IMyShizukuService? = null
    fun attachShizukuService(service: IMyShizukuService) {
        ScriptLogger.i(TAG, "attachShizukuService")
        ScriptLogger.i(TAG, "attachShizukuService - Hash: ${this.hashCode()}")
        this.shizukuService = service
    }

    fun detachShizukuService() {
        this.shizukuService = null
    }
    // ------------------ shizukuService end ------------------

    override fun handleAccessibilityEvent(event: AccessibilityEvent) {

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
                if (!isLaunchingAppLock) {
                    isLaunchingAppLock = true
                    driveByInnerState(State.WAIT_TO_OPEN_SIDE_BAR, 1500L)
                }

                // ------------------ 可能情况：出现广告，点击跳过广告后，重新将状态更改为 WAIT_TO_OPEN_SIDE_BAR ------------------
                if (packageName == APP_PACKAGE_NAME &&
                    className == "android.widget.RelativeLayout"
                ) {
                    ScriptLogger.i(TAG, "发现 APP 启动广告")
                    handler.removeCallbacksAndMessages(null)
                    handleClickSkipButton {
                        driveByInnerState(State.WAIT_TO_OPEN_SIDE_BAR, 1500L)
                    }
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
                    a11yService?.performActionGlobal()
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


            else -> {
                // 这里可能用于处理弹窗等情况会比较合适
                // TYPE_WINDOW_STATE_CHANGED className: com.netease.cloudmusic.ui.FeatureDialog 升级的弹窗
                // TYPE_WINDOW_STATE_CHANGED className: i83.a 升级完成之后的弹窗
            }
        }
    }

    fun startAutomation() {
        ScriptLogger.i(TAG, "startAutomation：启动脚本。")
        shizukuService?.openApp(APP_PACKAGE_NAME)

        driveByOuterState(State.LAUNCHING_APP)
    }

    fun stopAutomation() {
        ScriptLogger.i(TAG, "stopAutomation：停止脚本。")
        handler.removeCallbacksAndMessages(null)
        currentState = State.WAIT_TO_LAUNCH_APP
        currentClassName = ""
        isLaunchingAppLock = false
        isWaitToOpenSideBar = false
        isWaitToClickFreeButtonLock = false
        isWaitToLightUpPuzzleLock = false
        isWaitToReturnAppLock = false
    }

    fun handleClickSkipButton(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                a11yService?.findNodeByReourceId("com.netease.cloudmusic:id/skipBtn")
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
                a11yService?.findNodeByReourceId(sideBarResourceId)
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
                val DCResourceId = "DC_FlatList"
                val container = a11yService?.findNodeByReourceId(DCResourceId)
                a11yService?.findNodeByText(container, "免费听VIP歌曲")
            },
            executeAction = { listenFreeButton ->
                if (listenFreeButton != null) {
                    ScriptLogger.i(TAG, "handleClickFreeListen：找到免费听VIP歌曲按钮，尝试点击。")
                    val center = listenFreeButton.centerPoint()
                    center?.let { (x, y) ->
                        shizukuService?.tap(x, y)
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
                    a11yService?.findNodeByReourceId("com.netease.cloudmusic:id/rn_content")
                a11yService?.findNodeByText(container, "看视频，点亮拼图")
            },
            executeAction = { puzzleButton ->
                if (puzzleButton != null) {
                    ScriptLogger.i(TAG, "handleLightUpPuzzle：找到看视频，点亮拼图按钮，尝试点击。")
                    val center = puzzleButton.centerPoint()
                    center?.let { (x, y) ->
                        shizukuService?.tap(x, y)
                    }
                    callback?.invoke()
                } else {
                    ScriptLogger.i(TAG, "handleLightUpPuzzle：找不到看视频，点亮拼图按钮，退出程序。")
                    stopAutomation()
                }
            }
        )
    }


    private fun handleRewardWay(callback: (() -> Unit)? = null) {
        executeWithTimeoutRetry(
            findAction = {
                val adTitleResourceId = "com.netease.cloudmusic:id/tv_ad_bottom_enhance_main_title"
                a11yService?.findNodeByReourceId(adTitleResourceId)
            },
            executeAction = { titleNode ->
                if (titleNode != null) {
                    val text = titleNode.text?.toString()
                    if (text == "看15秒后点击") {
                        ScriptLogger.i(TAG, "handleRewardWay：看15秒后点击")
                        handleWaitAndClick(callback)
                    } else if (text == "点击跳转APP停留10秒") {
                        ScriptLogger.i(TAG, "handleRewardWay：点击跳转APP停留10秒")
                        handleClickAndWait(callback)
                    } else {
                        ScriptLogger.i(TAG, "handleRewardWay：不知名文本，退出程序。")
                        stopAutomation()
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
                    a11yService?.findNodeByReourceId("com.netease.cloudmusic:id/adConsumeClick")
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
                a11yService?.findNodeByReourceId("com.netease.cloudmusic:id/adConsumeClick")
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
            a11yService?.backToApp(APP_PACKAGE_NAME)
            handler.postDelayed({
                handleReturnApp(callback)
            }, 1500L)
        }
        if (currentClassName == "com.afollestad.materialdialogs.d" || currentClassName == "com.netease.cloudmusic.module.ad.motivation.commonui.AdMotivationVideoActivity") {
            ScriptLogger.i(TAG, "最后一次返回")
            a11yService?.performActionGlobal()
            callback?.invoke()
        } else {
            ScriptLogger.i(TAG, "还需执行返回")
            a11yService?.performActionGlobal()
            handler.postDelayed({
                handleReturnApp(callback)
            }, 1500L)
        }
    }


    private fun driveByInnerState(newState: State, delay: Long = 1000L) {
        handler.removeCallbacksAndMessages(null) // 清除所有延迟任务
        currentState = newState
        ScriptLogger.d(TAG, "driveByInnerState：newState：$newState")
        handler.postDelayed({ handleStateLogic() }, delay)
    }

    private fun driveByOuterState(newState: State) {
        ScriptLogger.d(TAG, "driveByOuterState：newState：$newState")
        handler.removeCallbacksAndMessages(null)
        currentState = newState
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

}

