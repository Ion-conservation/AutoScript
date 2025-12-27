package auto.script.A11yService

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import auto.script.di.entrypoints.A11yServiceEntryPoint
import auto.script.utils.ScriptLogger
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

@AndroidEntryPoint
class A11yServiceManager : AccessibilityService() {

    @Inject
    lateinit var eventDispatcher: A11yEventDispatcher
    private val entryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            A11yServiceEntryPoint::class.java
        )
    }

    private val a11yServiceRepository get() = entryPoint.a11yServiceRepository()

    private val TAG = "A11yServiceManager"

    // ------------------ AccessibilityService 生命周期 ------------------
    override fun onServiceConnected() {

        ScriptLogger.i(TAG, "A11yService connected. ")

        a11yServiceRepository.updateA11yServiceConnectState(true)
        a11yServiceRepository.attachServiceInstance(this)
        a11yServiceRepository.notifyConnected()
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        eventDispatcher.dispatch(event)

    }

    override fun onInterrupt() {
        ScriptLogger.d(TAG, "A11yService interrupted.")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        ScriptLogger.d(TAG, "onUnbind called. All internal clients have unbound.")
        a11yServiceRepository.updateA11yServiceConnectState(false)

        return super.onUnbind(intent)
    }

}