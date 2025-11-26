package auto.script.service

import android.view.accessibility.AccessibilityNodeInfo

interface A11yCapability {
    /** 暴露执行 UI 动作的方法，如点击、滑动等 */
    fun performActionGlobal(): Boolean

    fun findNodeByReourceId(resourceId: String): AccessibilityNodeInfo?

    fun findNodeByText(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo?

    fun findNodeByClassAndText(
        root: AccessibilityNodeInfo?,
        targetClass: String,
        targetText: String
    ): AccessibilityNodeInfo?

    fun backToApp()
}