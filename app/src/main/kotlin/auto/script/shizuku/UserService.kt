package auto.script.shizuku

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Xml
import android.view.accessibility.AccessibilityNodeInfo
import auto.script.common.ScriptCountdownManager.saveTargetTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedReader
import java.io.StringReader
import java.util.Calendar
import kotlin.system.exitProcess


class UserService : IShizukuService.Stub() {
    companion object {
        const val TAG = "UserService"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun logInMainThread(message: String) {
        Handler(Looper.getMainLooper()).post {
            Log.d(TAG, message)
        }
    }

    /**
     * 内部辅助函数：真正执行 Shell 命令的地方
     * 由于 UserService 运行在高权限进程中，直接使用 Runtime.exec 执行命令。
     * @return Pair(stdout, stderr)
     */
    private fun execShell(cmd: String): Pair<String, String> {
        try {
            // 1. 直接使用 Runtime 创建进程（因为已经在高权限上下文中）
            val process = Runtime.getRuntime().exec(cmd)
            logInMainThread("Executing: $cmd")

            // 2. 分别读取标准输出 (stdout) 和标准错误 (stderr)
            //    我们必须同时读取两者，否则如果缓冲区满了，进程会卡住！
            val stdout = process.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderr = process.errorStream.bufferedReader().use(BufferedReader::readText)

            // 3. 等待命令执行完毕
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                logInMainThread("$cmd 命令执行成功。")
            } else {
                logInMainThread("$cmd 命令执行失败，退出代号： $exitCode。")
            }
            // 4. 返回输出
            return Pair(stdout, stderr)
        } catch (e: Exception) {
            logInMainThread("$cmd 命令执行发生错误: ${e.message}")
            return Pair("", e.message ?: "Unknown Error")
        }
    }

    // --- 实现 AIDL 接口定义的方法 ---

    override fun openApp(packageName: String) {
        execShell("monkey -p $packageName 1")
    }

    override fun returnApp() {
        // 使用 am start 启动应用的 LAUNCHER Activity，比 monkey 更直接
        execShell("am start -n auto.script/.MainActivity")
    }

    override fun getUiXml(filename: String): String {
        val dumpFile = "/data/local/tmp/u$filename"
        // 1. 先 dump 布局
        execShell("uiautomator dump $dumpFile")
        // 2. 再读取 dump 出来的文件内容并返回
        val (xml, err) = execShell("cat $dumpFile")

        if (err.isNotEmpty()) {
            return "Error: $err"
        } else {
            return xml
        }
    }

    override fun tap(x: Int, y: Int) {
        execShell("input tap $x $y")
    }

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Int) {
        execShell("input swipe $x1 $y1 $x2 $y2 $duration")
    }

    override fun back() {
        // 4 是 KEYCODE_BACK 的值
        execShell("input keyevent 4")
    }

    override fun exit() {
        logInMainThread("UserService exit() called. Process will terminate.")
        // 优雅地关闭这个高权限进程
        exitProcess(0)
    }

    /**
     * filename 必传，其他参数可选。
     * */
    override fun getXMLAndTap(
        filename: String,
        targetResourceId: String,
        targetClass: String,
        targetText: String,
        contentDesc: String
    ) {
        val xml = getUiXml(filename)

        val targetBounds = findNodeBounds(
            xmlString = xml,
            resourceId = targetResourceId,
            className = targetClass,
            text = targetText,
            contentDesc = contentDesc
        )

        if (targetBounds != null) {
            tap(targetBounds.centerX(), targetBounds.centerY())
        } else {
            logInMainThread("找不到目标节点。")
        }
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
    override fun findNodeBounds(
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
                            logInMainThread(
                                "匹配到节点: text=$nodeText, desc=$nodeDesc, resId=$nodeResId, class=$nodeClass, bounds=$nodeBounds"
                            )
                            return rect
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            logInMainThread("XML parsing failed: ${e.message}")
        }
        return null
    }

    override fun findNodeBoundsByResourceId(xmlContent: String, targetId: String): Rect? {
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
                        logInMainThread("Found node at $nodeBounds")
                        return rect
                    }
                }
            }
            eventType = parser.next()
        }
        return null
    }

    override fun getBoundsByAccessibilityNodeInfo(node: AccessibilityNodeInfo): Rect? {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        val boundsString = "[${rect.left},${rect.top}][${rect.right},${rect.bottom}]"
        Log.i(TAG, boundsString)
        val bounds = parseBounds(boundsString)
        if (bounds != null) {
            logInMainThread("Found node at $bounds")
            return bounds
        }
        return null
    }

    /** 辅助函数：解析 "[x1,y1][x2,y2]" 格式的字符串 */
    override fun parseBounds(boundsString: String): Rect? {
        try {
            val parts = boundsString.replace("][", ",").replace("[", "").replace("]", "").split(",")
            if (parts.size == 4) {
                return Rect(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
            }
        } catch (e: NumberFormatException) {
            logInMainThread("parse bounds failed: $e")
        }
        return null
    }

    override fun parseRemainingTimeAndSave(xmlString: String) {
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


}