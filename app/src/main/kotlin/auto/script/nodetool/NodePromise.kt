package auto.script.nodetool

import NodeResult
import android.os.Handler

class NodePromise(
    private val handler: Handler,
    private val finder: NodeFinder
) {

    // -------------------------
    // retry 配置
    // -------------------------
    private data class RetryConfig(
        val timeoutMills: Long,
        val delayMills: Long
    )

    private var retryConfig: RetryConfig? = null

    // then / catch 回调
    private var thenBlock: ((NodeResult?) -> Unit)? = null
    private var catchBlock: ((Throwable) -> Unit)? = null

    // execute 函数引用（retry 会覆盖它）
    private var execute: () -> Unit = { executeDefault() }


    // -------------------------
    // retry：使用 tryFind()（A11y → Shizuku）
    // -------------------------
    fun retry(timeout: Long = 2000L, delay: Long = 500L): NodePromise {
        return NodePromise(handler, finder).also { promise ->

            promise.retryConfig = RetryConfig(timeout, delay)

            promise.execute = {
                val start = System.currentTimeMillis()

                val runnable = object : Runnable {
                    override fun run() {
                        try {
                            val result = promise.tryFind()

                            if (result != null) {
                                promise.thenBlock?.invoke(result)
                                return
                            }

                            val elapsed = System.currentTimeMillis() - start
                            if (elapsed < timeout) {
                                handler.postDelayed(this, delay)
                            } else {
                                promise.thenBlock?.invoke(null)
                            }

                        } catch (e: Throwable) {
                            promise.catchBlock?.invoke(e)
                        }
                    }
                }

                handler.post(runnable)
            }
        }
    }


    // -------------------------
    // then / catch
    // -------------------------
    fun then(block: (NodeResult?) -> Unit): NodePromise {
        this.thenBlock = block
        execute()
        return this
    }

    fun catch(block: (Throwable) -> Unit): NodePromise {
        this.catchBlock = block
        return this
    }


    // -------------------------
    // tryFind：A11y → Shizuku 双重兜底
    // -------------------------
    private fun tryFind(): NodeResult? {
        // 1. A11y
        finder.findByA11y()?.let { node ->
            return NodeResult.A11yNode(node)
        }

        // 2. Shizuku
        finder.findByShizuku()?.let { shizukuNode ->
            return shizukuNode
        }

        return null
    }


    // -------------------------
    // execute（无 retry 时）
    // -------------------------
    private fun executeDefault() {
        try {
            val result = tryFind()
            thenBlock?.invoke(result)
        } catch (e: Throwable) {
            catchBlock?.invoke(e)
        }
    }
}
