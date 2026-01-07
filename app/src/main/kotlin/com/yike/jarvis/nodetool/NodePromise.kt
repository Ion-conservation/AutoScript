package com.yike.jarvis.nodetool

import NodeResult
import android.os.Handler

class NodePromise(
    private val handler: Handler,
    private val finder: NodeFinder
) {

    private var timeoutMills: Long = 2000L
    private var delayMills: Long = 500L
    private var isRetryMode = true

    private var thenBlock: ((NodeResult) -> Unit)? = null
    private var failBlock: (() -> Unit)? = null
    private var catchBlock: ((Throwable) -> Unit)? = null

    // 1. 配置重试参数（仅记录参数，不执行）
    fun retry(timeout: Long = 2000L, delay: Long = 500L): NodePromise {
        this.timeoutMills = timeout
        this.delayMills = delay
        this.isRetryMode = true
        return this
    }

    // 2. 配置成功回调
    fun then(block: (NodeResult) -> Unit): NodePromise {
        this.thenBlock = block
        return this
    }


    fun fail(block: () -> Unit): NodePromise {
        this.failBlock = block
        return this
    }


    // 3. 配置失败回调
    fun catch(block: (Throwable) -> Unit): NodePromise {
        this.catchBlock = block
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
                    if (result == null) {
                        failBlock?.invoke()
                        return@post
                    } else {
                        thenBlock?.invoke(result)
                    }

                }
            } catch (e: Throwable) {
                handler.post {
                    catchBlock?.invoke(e)
                }
            }
        }.start()
    }

    fun tryFind(): NodeResult? {
        return finder.findWithRetry()
    }
}