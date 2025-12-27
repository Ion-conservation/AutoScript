package auto.script.A11yService

import android.view.accessibility.AccessibilityEvent
import auto.script.feature.netease.NeteaseExecutor
import auto.script.feature.taobao.TaobaoExecutor
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
