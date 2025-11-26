package auto.script.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import auto.script.executor.CloudmusicExecutor
import auto.script.executor.TaobaoExecutor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AutomationService @Inject constructor() : AccessibilityService(), A11yCapability {

    @Inject
    lateinit var automationServiceRepo: AutomationServiceRepo

    @Inject
    lateinit var taobaoExecutor: TaobaoExecutor

    @Inject
    lateinit var cloudmusicExecutor: CloudmusicExecutor


    private val TAG = "AutomationService"


    // ------------------ AccessibilityService 生命周期 ------------------

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "A11yService connected. Starting Foreground Service...")

        automationServiceRepo.updateA11yServiceConnected(true)

        taobaoExecutor.attachA11yService(this)
        cloudmusicExecutor.attachA11yService(this)

        backToApp()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 分发事件
        if (event == null) return

        taobaoExecutor.handleAccessibilityEvent(event)
        cloudmusicExecutor.handleAccessibilityEvent(event)
    }

    override fun onInterrupt() {
        Log.w(TAG, "A11yService interrupted.")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind called. All internal clients have unbound.")
        automationServiceRepo.updateA11yServiceConnected(false)

        taobaoExecutor.detachA11yService()
        cloudmusicExecutor.detachA11yService()
        return super.onUnbind(intent)
    }


    // ------------------ AccessibilityService 核心方法 ------------------
    override fun performActionGlobal(): Boolean {
        Log.i(TAG, "接收到 来自 executor 的 performAction click")
        // **这里是关键：直接调用父类的 final 方法**
        return super.performGlobalAction(GLOBAL_ACTION_BACK)
    }


    override fun findNodeByReourceId(resourceId: String): AccessibilityNodeInfo? {
        return rootInActiveWindow.findAccessibilityNodeInfosByViewId(resourceId)
            .firstOrNull()
    }

    override fun findNodeByText(
        root: AccessibilityNodeInfo?,
        text: String
    ): AccessibilityNodeInfo? {
        return rootInActiveWindow.findAccessibilityNodeInfosByText(text)
            .firstOrNull()
    }

    /**
     * 在指定节点树中查找第一个匹配的元素
     * @param root 根节点（通常是 service.rootInActiveWindow）
     * @param targetClass 目标控件的类名，例如 "android.widget.Button"
     * @param targetText 目标控件的文本，例如 "确定"
     * @return 第一个匹配的 AccessibilityNodeInfo 或 null
     */
    override fun findNodeByClassAndText(
        root: AccessibilityNodeInfo?,
        targetClass: String,
        targetText: String
    ): AccessibilityNodeInfo? {
        if (root == null) return null

        val queue: ArrayDeque<AccessibilityNodeInfo> = ArrayDeque()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()

            val classMatch = node.className?.toString() == targetClass
            val textMatch = node.text?.toString() == targetText

            if (classMatch && textMatch) {
                return node
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    override fun backToApp() {
        val intent = packageManager.getLaunchIntentForPackage("auto.script")
        intent.let {
            // 确保 Activity 被带到前台
            it?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(it)
            Log.i(TAG, "正在回到脚本...")
        }
    }

}