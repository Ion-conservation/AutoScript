package auto.script

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class TaobaoService : AccessibilityService() {
    companion object {
        private const val TAG = "TaobaoAutomation"
        private const val ACTION_START_AUTOMATION = "auto.script.taobao.START_AUTOMATION"
        private const val ACTION_STOP_AUTOMATION = "auto.script.taobao.STOP_AUTOMATION"

        fun getStartIntent(context: Context): Intent {
            return Intent(context, TaobaoService::class.java).apply {
                setAction(ACTION_START_AUTOMATION)
            }
        }

        fun getStopIntent(context: Context): Intent {
            return Intent(context, TaobaoService::class.java).apply {
                setAction(ACTION_STOP_AUTOMATION)
            }
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.i(TAG, "通过 startService 命令接收到启动信号。")

    }

    override fun onInterrupt() {
        Log.w(TAG, "服务被中断。")
    }

}