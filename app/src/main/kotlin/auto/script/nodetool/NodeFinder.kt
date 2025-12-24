package auto.script.nodetool

import NodeResult
import android.view.accessibility.AccessibilityNodeInfo

abstract class NodeFinder {
    abstract fun findByA11y(): AccessibilityNodeInfo?
    abstract fun findByShizuku(): NodeResult.ShizukuNode?
}


class IdFinder( private val resId: String) : NodeFinder() {
    override fun findByA11y(): AccessibilityNodeInfo? {
        return NodeContext.a11yServiceTool.findNodeByReourceId( resId)
    }

    override fun findByShizuku(): NodeResult.ShizukuNode? {
        return NodeContext.shizukuServiceTool.findNodeById(resId)
    }
}

class TextFinder( private val text: String) : NodeFinder() {

    override fun findByA11y(): AccessibilityNodeInfo? {
        return NodeContext.a11yServiceTool.findNodeByText(null, text)
    }

    override fun findByShizuku(): NodeResult.ShizukuNode? {
        return  NodeContext.shizukuServiceTool.findNodeByText(text)
    }
}


