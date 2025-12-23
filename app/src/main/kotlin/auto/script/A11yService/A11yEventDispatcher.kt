package auto.script.A11yService

import android.view.accessibility.AccessibilityEvent
import auto.script.executor.CloudmusicExecutor
import auto.script.executor.TaobaoExecutor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class A11yEventDispatcher @Inject constructor(
    private val taobaoExecutor: TaobaoExecutor,
    private val cloudmusicExecutor: CloudmusicExecutor
) {
    fun dispatch(event: AccessibilityEvent) {
        taobaoExecutor.handleAccessibilityEvent(event)
        cloudmusicExecutor.handleAccessibilityEvent(event)
    }
}
