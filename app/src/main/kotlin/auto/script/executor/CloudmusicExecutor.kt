package auto.script.executor

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import auto.script.common.EventTaskHandler
import auto.script.common.centerPoint
import auto.script.service.A11yCapability
import auto.script.shizuku.IAssistService
import auto.script.utils.ScriptUtils
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudmusicExecutor @Inject constructor(
    private val a11yCapability: A11yCapability,
    private val shizukuService: IAssistService
) : EventTaskHandler {

//    @Inject
//    lateinit var a11yCapability: A11yCapability
//
//    @Inject
//    lateinit var shizukuService: IAssistService

    companion object {
        private const val TAG = "网易云音乐脚本"
        private const val APP_PACKAGE_NAME = "com.netease.cloudmusic"
        private var hadClickFreeListenButton = false // 标记是否第一次，避免重复滚动等其他操作

        private val handler = Handler(Looper.getMainLooper())
        private var isScrolling = false // 防止重复滚动

        // 使用一个简单的状态机来管理自动化流程
        private var currentState = State.IDLE
        private var currentActivity = ""

        private var FINISH_TEXT = "已全部点亮，明天再来"

        // 自动化流程的状态枚举
        private enum class State {
            IDLE,                   // 空闲状态，等待应用启动
            LAUNCHING_APP, //  正在启动 APP，通过 onAccessibilityEvent 进入下一步
            WAIT_TO_CLICK_SKIP_BUTTON, // 等待点击 跳过 按钮

            CHECK_IF_DIALOG, // 检查弹窗
            OPEN_SIDE_BAR, // 通过打开抽屉来进入免费听
            WAIT_TO_CLICK_FREE_BUTTON,   // 点击 “免费听” 按钮

            LIGHT_UP_PUZZLE, // 找到 ”看视频，点亮拼图“ 按钮，并点击
            HANDLE_AD_TITLE, // 点击广告，进入详情页

            // 看15秒后点击
            CLICK_AD_AFTER_15_SECOND,
            CHECK_IF_CANCEL_BUTTON,
            CHECK_IF_CANCEL_BUTTON_AND_WAIT,
            RETURN_TO_APP,
            CLICK_AD_IMMEDIATE

        }


    }

    private val isRunning = AtomicBoolean(false)
    override fun isTaskActive(): Boolean = isRunning.get()

    override fun handleAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(TAG, "IN handleAccessibilityEvent")
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            Log.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED packageName: ${event.packageName}")
            Log.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED className: ${event.className.toString()}")

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


    /**
     * 核心状态处理逻辑
     */
    private fun handleStateLogic() {

        when (currentState) {

            State.WAIT_TO_CLICK_SKIP_BUTTON -> handleClickSkipButton()

            State.CHECK_IF_DIALOG -> handleCheckDialog()

            State.OPEN_SIDE_BAR -> handleOpenSideBar()
            // 3. 进入 APP 首页，查找 ”免费听“ 按钮，并点击
            State.WAIT_TO_CLICK_FREE_BUTTON -> handleClickFreeListen()

            State.LIGHT_UP_PUZZLE -> handleLightUpPuzzle()
            // 6. 点击广告，进入详情页
            State.HANDLE_AD_TITLE -> handleAdTitle()

            // 看15秒后点击
            State.CLICK_AD_AFTER_15_SECOND -> handleClickADAfter15Second()
            // 检查取消按钮
            State.CHECK_IF_CANCEL_BUTTON -> handleCheckCancelButton(2000L)
            State.CHECK_IF_CANCEL_BUTTON_AND_WAIT -> handleCheckCancelButton(8000L)
            // 结合 activity 返回 APP
            State.RETURN_TO_APP -> handleReturnApp()


            // 点击跳转APP停留10秒
            State.CLICK_AD_IMMEDIATE -> handleClickADImmediateAndWait10Second()
            else -> { /* 其它状态由内部逻辑驱动，不直接由事件触发 */
            }
        }
    }

    fun startAutomation() {
        isRunning.set(true)

        shizukuService.openApp(APP_PACKAGE_NAME)

        driveByOuterState(State.LAUNCHING_APP)
    }

    fun stopAutomation() {
        isRunning.set(false)
    }

    fun handleClickSkipButton() {
        executeWithTimeoutRetry(
            description = "步骤 2 ：查找 APP 启动广告的 ‘跳过’ 按钮并点击。",
            findAction = {
                a11yCapability.findNodeByReourceId("com.netease.cloudmusic:id/skipBtn")
            },
            executeAction = { skipButton ->
                if (skipButton != null) {
                    Log.i(TAG, "步骤 2 结果：找到 App 启动广告 '跳过' 按钮，尝试点击。")
                    skipButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                } else {
                    Log.i(TAG, "步骤 2 结果：找不到 App 启动广告 '跳过' 按钮，等待下一个状态。")
                }

                driveByInnerState(State.CHECK_IF_DIALOG, 2000)
            },
        )
    }

    private fun handleCheckDialog() {
        executeWithTimeoutRetry(
            description = "步骤 3 ：检查弹窗。",
            findAction = {
                val dialogResourceId = "com.netease.cloudmusic:id/dsl_dialog_root"
                a11yCapability.findNodeByReourceId(dialogResourceId)
            },
            executeAction = { dialog ->
                if (dialog != null) {
                    Log.i(TAG, "步骤 3 结果：检测到弹窗，执行 返回 动作关闭弹窗。")
                    a11yCapability.performActionGlobal()
                } else {
                    Log.i(TAG, "步骤 3 结果：找不到 弹窗，执行下一步。")

                }
                driveByInnerState(State.OPEN_SIDE_BAR, 2000)
            },
        )
    }

    private fun handleOpenSideBar() {
        executeWithTimeoutRetry(
            description = "步骤 4 ：打开侧边栏菜单。",
            findAction = {
                val sideBarResourceId = "com.netease.cloudmusic:id/menu_icon_container"
                a11yCapability.findNodeByReourceId(sideBarResourceId)
            },
            executeAction = { sideBarButton ->
                if (sideBarButton != null) {
                    Log.i(TAG, "步骤 4 结果：找到 侧边栏菜单按钮，点击。")
                    sideBarButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)

                    driveByInnerState(State.WAIT_TO_CLICK_FREE_BUTTON, 2000)
                } else {
                    Log.i(TAG, "步骤 42 结果：退出程序。")
                    stopAutomation()
                }
            }
        )
    }

    private fun handleClickFreeListen() {
        executeWithTimeoutRetry(
            description = "步骤 5 ：找到 侧边栏菜单 中的文本 ‘免费听VIP歌曲’ 并点击。",
            findAction = {
                val DCResourceId = "DC_FlatList"
                val container = a11yCapability.findNodeByReourceId(DCResourceId)
                a11yCapability.findNodeByText(container, "免费听VIP歌曲")
            },
            executeAction = { listenFreeButton ->
                if (listenFreeButton != null) {
                    Log.i(TAG, "步骤 5 结果：找到 ‘免费听VIP歌曲’ 按钮。")
                    val center = listenFreeButton.centerPoint()
                    center?.let { (x, y) ->
                        shizukuService.tap(x, y)
                    }

                    driveByInnerState(State.LIGHT_UP_PUZZLE, 2000)
                } else {
                    Log.i(TAG, "步骤 5 结果：查找 ‘免费听VIP歌曲’ 按钮失败，退出程序。")
                    stopAutomation()
                }
            }
        )
    }


    private fun handleLightUpPuzzle() {

        val xml = shizukuService.getUiXml("rest_time.xml")

        ScriptUtils.saveXmlToLocal(xml, "rest_time.xml")


        executeWithTimeoutRetry(
            description = "步骤 6 ：查找 ‘看视频，点亮拼图’ 按钮并点击。",
            findAction = {
                val container =
                    a11yCapability.findNodeByReourceId("com.netease.cloudmusic:id/rn_content")
                a11yCapability.findNodeByText(container, "看视频，点亮拼图")
            },
            executeAction = { puzzleButton ->
                if (puzzleButton != null) {
                    Log.i(TAG, "步骤 6 结果：找到 ‘免费听VIP歌曲’ 按钮。")
                    val center = puzzleButton.centerPoint()
                    center?.let { (x, y) ->
                        shizukuService.tap(x, y)
                    }

                    driveByInnerState(State.HANDLE_AD_TITLE, 2000)
                } else {
                    Log.i(TAG, "步骤 6 结果：找不到 ‘看视频，点亮拼图’ 按钮，退出程序。")
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
                a11yCapability.findNodeByReourceId(adTitleResourceId)
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

    private fun handleClickADAfter15Second() {
        handler.postDelayed({
            executeWithTimeoutRetry(
                description = "15 秒时间到，点击广告领取奖励。",
                findAction = {
                    a11yCapability.findNodeByReourceId("com.netease.cloudmusic:id/adConsumeClick")
                },
                executeAction = { adButton ->
                    adButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    driveByInnerState(State.CHECK_IF_CANCEL_BUTTON, 2000L)
                },
            )
        }, 13000)
    }

    private fun handleCheckCancelButton(delay: Long) {
        executeWithTimeoutRetry(
            description = "点击广告详情页的 取消 按钮。",
            findAction = {
//                val container =
//                    a11yCapability.findNodeByText(container, "取消")
                a11yCapability.findNodeByReourceId("android:id/button2")
            },
            executeAction = { cancelButton ->
                if (cancelButton == null) {
                    Log.i(TAG, "找不到取消按钮。")
                }
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
            driveByInnerState(State.LIGHT_UP_PUZZLE)
        } else {
            a11yCapability.performActionGlobal()
            handler.postDelayed({
                handleReturnApp()
            }, 1000)
        }
    }

    private fun handleClickADImmediateAndWait10Second() {
        executeWithTimeoutRetry(
            description = "点击跳转APP停留10秒。",
            findAction = {
                a11yCapability.findNodeByReourceId("com.netease.cloudmusic:id/adConsumeClick")
            },
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

    private fun executeWithTimeoutRetry(
        description: String = "",
        timeoutMills: Long = 2000L,
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

