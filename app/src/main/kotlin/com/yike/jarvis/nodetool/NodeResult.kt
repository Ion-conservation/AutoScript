import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.yike.jarvis.nodetool.NodeContext
import com.yike.jarvis.utils.ScriptLogger

sealed class NodeResult {

    abstract val text: String?
    
    // 添加获取中心点的抽象方法
    abstract fun getCenterPoint(): Pair<Int, Int>?

    class A11yNode(val info: AccessibilityNodeInfo) : NodeResult() {
        override val text: String? get() = info.text?.toString()
        
        override fun getCenterPoint(): Pair<Int, Int>? {
            val rect = Rect()
            info.getBoundsInScreen(rect)
            if (rect.isEmpty) return null
            val centerX = (rect.left + rect.right) / 2
            val centerY = (rect.top + rect.bottom) / 2
            return Pair(centerX, centerY)
        }
    }

    class ShizukuNode(val x: Int, val y: Int, override val text: String?) : NodeResult() {
        override fun getCenterPoint(): Pair<Int, Int>? {
            return Pair(x, y)
        }
    }

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
