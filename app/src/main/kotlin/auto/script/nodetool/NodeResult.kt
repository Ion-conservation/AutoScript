
import android.view.accessibility.AccessibilityNodeInfo
import auto.script.nodetool.NodeContext

sealed class NodeResult {

    class A11yNode(val node: AccessibilityNodeInfo) : NodeResult()
    class ShizukuNode(val x: Int, val y: Int) : NodeResult()

    fun click() {
        when (this) {
            is A11yNode -> node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            is ShizukuNode -> NodeContext.shizukuServiceTool.tap(x, y)
        }
    }
}
