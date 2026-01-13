package com.yike.jarvis.core.a11y.service

import android.view.accessibility.AccessibilityEvent
import com.yike.jarvis.feature.netease.executor.NeteaseExecutor
import com.yike.jarvis.feature.taobao.TaobaoExecutor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class A11yEventDispatcher @Inject constructor(
    private val taobaoExecutor: TaobaoExecutor,
    private val neteaseExecutor: NeteaseExecutor
) {
    fun dispatch(event: AccessibilityEvent) {
        taobaoExecutor.handleAccessibilityEvent(event)
        neteaseExecutor.handleAccessibilityEvent(event)
    }
}
