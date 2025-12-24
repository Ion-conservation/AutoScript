package auto.script.A11yService

import android.view.accessibility.AccessibilityNodeInfo

interface IA11yServiceTool {

    /** 暴露执行 UI 动作的方法，如点击、滑动等 */

    fun getRootNode(): AccessibilityNodeInfo?
    fun performActionGlobal(): Boolean

    fun findNodeByReourceId(resourceId: String): AccessibilityNodeInfo?

    fun findNodeByText(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo?

    fun findNodeByClassAndText(
        root: AccessibilityNodeInfo?,
        targetClass: String,
        targetText: String
    ): AccessibilityNodeInfo?

}