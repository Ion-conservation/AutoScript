package auto.script.utils

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

object ScriptUtils {

    val TAG = "ScriptUtils"

    /**
     * 检查辅助功能服务是否启用
     */
    fun isAccessibilityServiceEnabled(
        context: Context,
        serviceClass: Class<out AccessibilityService>
    ): Boolean {
        Log.i(TAG, "${context.packageName}/${serviceClass.canonicalName}")

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        Log.d(TAG, "Enabled services raw: $enabledServices")

        val serviceId = "${context.packageName}/${serviceClass.canonicalName}"
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
            accessibilityEnabled == 1 && enabledServices.contains(serviceId)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 引导用户到系统的辅助功能设置页面
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * 默认在 5 秒内重复尝试执行 findAction，成功则执行 onSuccess，失败则执行 onFailure
     */
    fun executeWithTimeoutRetry(
        handler: Handler,
        description: String = "",
        timeoutMills: Long = 5000L,
        delayMills: Long = 500L,
        checkBeforeAction: () -> Unit = {},
        findAction: () -> AccessibilityNodeInfo?,
        onSuccess: (AccessibilityNodeInfo) -> Unit,
        onFailure: () -> Unit
    ) {
        if (description.isNotEmpty()) {
            Log.i(TAG, description)
        }

        val startTime = System.currentTimeMillis()

        val retryRunnable = object : Runnable {
            override fun run() {
                // 专门用于检查各种弹窗
                checkBeforeAction()

                val result = findAction()
                if (result != null) {
                    handler.removeCallbacks(this)
                    onSuccess(result)
                } else {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < timeoutMills) {
                        handler.postDelayed(this, delayMills)
                    } else {
                        handler.removeCallbacks(this)
                        onFailure()
                    }
                }
            }
        }
        handler.post(retryRunnable)
    }

    fun logAndToast(context: Context, TAG: String, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        Log.i(TAG, message)
    }
}


