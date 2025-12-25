import android.view.accessibility.AccessibilityNodeInfo
import auto.script.nodetool.NodeContext
import auto.script.utils.ScriptLogger

sealed class NodeResult {

    abstract val text: String?

    class A11yNode(val info: AccessibilityNodeInfo) : NodeResult() {
        override val text: String? get() = info.text?.toString()
    }

    class ShizukuNode(val x: Int, val y: Int, override val text: String?) : NodeResult()

    fun click() {
        when (this) {
            is A11yNode -> {
                ScriptLogger.i("NodeResult", "A11y click")
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }

            is ShizukuNode -> {
                ScriptLogger.i("NodeResult", "shizuku click")
                NodeContext.shizukuServiceTool.tap(x, y)
            }
        }
    }
}
