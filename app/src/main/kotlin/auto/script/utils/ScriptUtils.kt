package auto.script.utils

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Handler
import android.provider.Settings
import android.util.Xml
import android.view.accessibility.AccessibilityNodeInfo
import auto.script.MyApp
import auto.script.common.ScriptCountdownManager.saveTargetTime
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.StringReader
import java.util.Calendar

object ScriptUtils {

    const val TAG = "ScriptUtils"

    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun saveXmlToLocal(
        xmlContent: String,
        fileName: String = "data.xml"
    ) {
        val file = File(MyApp.instance.filesDir, fileName)
        file.writeText(xmlContent, Charsets.UTF_8)
    }

    fun getCenterFromBounds(
        topLeft: Pair<Int, Int>,
        bottomRight: Pair<Int, Int>
    ): Pair<Int, Int> {
        val (x1, y1) = topLeft
        val (x2, y2) = bottomRight
        val centerX = (x1 + x2) / 2
        val centerY = (y1 + y2) / 2
        return Pair(centerX, centerY)
    }

    fun getCenterFromNodeInfo(node: AccessibilityNodeInfo): Pair<Int, Int>? {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        if (rect.isEmpty) return null
        return Pair(rect.centerX(), rect.centerY())
    }


    /**
     * 检查辅助功能服务是否启用
     */
    fun isAccessibilityServiceEnabled(
        context: Context,
        serviceClass: Class<out AccessibilityService>
    ): Boolean {
        ScriptLogger.i(TAG, "${context.packageName}/${serviceClass.canonicalName}")

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        ScriptLogger.d(TAG, "Enabled services raw: $enabledServices")

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
            ScriptLogger.i(TAG, description)
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

    /**
     * 启动普通 APP
     * 通过 PackageManager 找到 APP 的 Activity 入口并启动。
     * */
    fun openApp(packageName: String) {
        val pm = appContext.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)

        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            appContext.startActivity(it)
            ScriptLogger.i(TAG, "正在打开 $packageName App...")
        }
    }

    /**
     * 启动无障碍设置
     * ActivityContext 可以直接启动 Activity
     * 在 Application / Service / BroadcastReceiver 里调用 必须加 FLAG_ACTIVITY_NEW_TASK，否则会崩溃。
     * */
    fun openA11yServiceSetting() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }

    fun onOpenShizukuApp() {
        openApp("moe.shizuku.privileged.api")
    }

    /** 辅助函数：解析 "[x1,y1][x2,y2]" 格式的字符串 */
    fun parseBounds(boundsString: String): Rect? {
        try {
            val parts = boundsString.replace("][", ",").replace("[", "").replace("]", "").split(",")
            if (parts.size == 4) {
                return Rect(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
            }
        } catch (e: NumberFormatException) {
            ScriptLogger.e(TAG, "parse bounds failed: $e")
        }
        return null
    }

    fun parseRemainingTimeAndSave(xmlString: String) {
        try {
            val parser = Xml.newPullParser()
            parser.setInput(StringReader(xmlString))

            var eventType = parser.eventType
            var days: Int = 0
            var hours: Int = 0
            var minutes: Int = 0

            // 用一个列表保存最近遇到的数字节点
            var lastNumber: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "node") {
                    val textValue = parser.getAttributeValue(null, "text") ?: ""

                    // 如果是纯数字，先暂存
                    if (textValue.matches(Regex("\\d+"))) {
                        lastNumber = textValue
                    }

                    // 如果是单位节点，根据前一个数字赋值
                    when (textValue) {
                        "天" -> {
                            days = lastNumber?.toIntOrNull() ?: 0
                            lastNumber = null
                        }

                        "时" -> {
                            hours = lastNumber?.toIntOrNull() ?: 0
                            lastNumber = null
                        }

                        "分" -> {
                            minutes = lastNumber?.toIntOrNull() ?: 0
                            lastNumber = null
                        }
                    }
                }
                eventType = parser.next()
            }

            // 计算目标时间
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis
                add(Calendar.DAY_OF_YEAR, days)
                add(Calendar.HOUR_OF_DAY, hours)
                add(Calendar.MINUTE, minutes)
            }

            val targetHour = target.get(Calendar.HOUR_OF_DAY)
            val targetMinute = target.get(Calendar.MINUTE)

            // 保存目标时间
            saveTargetTime(targetHour, targetMinute)

            android.util.Log.i(
                "ScriptCountdown",
                "解析到剩余时间: ${days}天 ${hours}时 ${minutes}分, 目标时间=${targetHour}:${targetMinute}"
            )
        } catch (e: Exception) {
            android.util.Log.e("ScriptCountdown", "XML解析失败: ${e.message}")
        }
    }

    fun findNodeBoundsByResourceId(xmlContent: String, targetId: String): Rect? {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setInput(StringReader(xmlContent))

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "node") {
                val resId = parser.getAttributeValue(null, "resource-id")
                if (resId == targetId) {
                    val nodeBounds = parser.getAttributeValue(null, "bounds")
                    val rect = parseBounds(nodeBounds)
                    if (rect != null) {
                        ScriptLogger.e(TAG, "Found node at $nodeBounds")
                        return rect
                    }
                }
            }
            eventType = parser.next()
        }
        return null
    }

    /**
     * 辅助函数：解析 XML 并查找节点
     * @param xmlString 从 Shizuku service 获取的 UI XML
     * @param resourceId 要查找的 resource-id 属性
     * @param className 要查找的 class 属性
     * @param text 要查找的 text 属性
     * @param contentDesc 要查找的 content-desc 属性
     * @return 节点的 bounds (Rect)，如果没找到则返回 null
     */
    fun findNodeBounds(
        xmlString: String,
        resourceId: String,
        className: String,
        text: String,
        contentDesc: String
    ): Rect? {
        try {
            val parser = Xml.newPullParser()
            parser.setInput(StringReader(xmlString))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "node") {
                    val nodeText = parser.getAttributeValue(null, "text") ?: ""
                    val nodeDesc = parser.getAttributeValue(null, "content-desc") ?: ""
                    val nodeResId = parser.getAttributeValue(null, "resource-id") ?: ""
                    val nodeClass = parser.getAttributeValue(null, "class") ?: ""
                    val nodeBounds = parser.getAttributeValue(null, "bounds")

                    val match =
                        (text.isNotEmpty() && nodeText == text) ||
                                (contentDesc.isNotEmpty() && nodeDesc == contentDesc) ||
                                (resourceId.isNotEmpty() && nodeResId == resourceId) ||
                                (className.isNotEmpty() && nodeClass == className)

                    if (match && nodeBounds != null) {
                        val rect = parseBounds(nodeBounds)
                        if (rect != null) {
                            ScriptLogger.e(
                                TAG,
                                "匹配到节点: text=$nodeText, desc=$nodeDesc, resId=$nodeResId, class=$nodeClass, bounds=$nodeBounds"
                            )
                            return rect
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            ScriptLogger.e(TAG, "XML parsing failed: ${e.message}")
        }
        return null
    }

}


