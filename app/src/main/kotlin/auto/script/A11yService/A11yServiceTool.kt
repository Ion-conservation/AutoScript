package auto.script.A11yService

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.view.accessibility.AccessibilityNodeInfo
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class A11yServiceTool @Inject constructor(
    private val a11yServiceRepository: A11yServiceRepository
) : IA11yServiceTool {

    private val TAG = "A11YServiceServiceTool"


    // ------------------ AccessibilityService 核心方法 ------------------

    override fun getRootNode(): AccessibilityNodeInfo? {
        return a11yServiceRepository.withService { a11yServiceInstance ->
            a11yServiceInstance.rootInActiveWindow
        }
    }


    override fun performActionGlobal(): Boolean {
        return a11yServiceRepository.withService { a11yServiceInstance ->
            a11yServiceInstance.performGlobalAction(GLOBAL_ACTION_BACK)
        } ?: false
    }

    override fun findNodeByReourceId(resourceId: String): AccessibilityNodeInfo? {
        return a11yServiceRepository.withService { a11yServiceInstance ->
            a11yServiceInstance.rootInActiveWindow?.findAccessibilityNodeInfosByViewId(
                resourceId
            )
                ?.firstOrNull()
        }
    }

    override fun findNodeByText(
        root: AccessibilityNodeInfo?,
        text: String
    ): AccessibilityNodeInfo? {
        return a11yServiceRepository.withService { a11yServiceInstance ->
            a11yServiceInstance.rootInActiveWindow?.findAccessibilityNodeInfosByText(text)
                ?.firstOrNull()
        }
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



}