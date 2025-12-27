package auto.script.common

import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

interface EventTaskHandler {

    fun handleAccessibilityEvent(event: AccessibilityEvent)
}

fun AccessibilityNodeInfo.getCenterPoint(): Pair<Int, Int>? {
    val rect = Rect()
    getBoundsInScreen(rect)
    if (rect.isEmpty) return null
    val centerX = (rect.left + rect.right) / 2
    val centerY = (rect.top + rect.bottom) / 2
    return Pair(centerX, centerY)
}
