package auto.script.nodetool

import NodeResult
import android.os.Handler

class NodePromise(
    private val handler: Handler,
    private val finder: NodeFinder
) {
    private var thenBlock: ((NodeResult?) -> Unit)? = null
    private var catchBlock: ((Throwable) -> Unit)? = null

    // 现在 then 只是一个简单的回调注册
    fun then(block: (NodeResult?) -> Unit): NodePromise {
        this.thenBlock = block
        return this
    }

    // start 方法现在只负责单次执行
    fun start() {
        // 建议在子线程执行查找，避免卡顿
        Thread {
            try {
                val result = tryFind()
                // 回到主线程执行回调
                handler.post {
                    thenBlock?.invoke(result)
                }
            } catch (e: Throwable) {
                handler.post {
                    catchBlock?.invoke(e)
                }
            }
        }.start()
    }

    private fun tryFind(): NodeResult? {
        // A11y 查找
        finder.findByA11y()?.let { return NodeResult.A11yNode(it) }
        // Shizuku 查找
        finder.findByShizuku()?.let { return it }
        return null
    }

}