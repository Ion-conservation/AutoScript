package auto.script.shizuku

import android.graphics.Rect
import android.util.Log
import android.util.Xml
import android.view.accessibility.AccessibilityNodeInfo
import auto.script.utils.ScriptUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedReader
import java.io.StringReader
import kotlin.system.exitProcess

class UserService : IAssistService.Stub() {
    companion object {
        const val TAG = "UserService"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * 内部辅助函数：真正执行 Shell 命令的地方
     * 由于 UserService 运行在高权限进程中，直接使用 Runtime.exec 执行命令。
     * @return Pair(stdout, stderr)
     */
    private fun execShell(cmd: String): Pair<String, String> {
//        withContext(Dispatchers.IO) {
        try {
            // 1. 直接使用 Runtime 创建进程（因为已经在高权限上下文中）
            val process = Runtime.getRuntime().exec(cmd)
            Log.d(TAG, "Executing: $cmd")

            // 2. 分别读取标准输出 (stdout) 和标准错误 (stderr)
            //    我们必须同时读取两者，否则如果缓冲区满了，进程会卡住！
            val stdout = process.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderr = process.errorStream.bufferedReader().use(BufferedReader::readText)

            // 3. 等待命令执行完毕
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Log.d(TAG, "Command executed successfully")
            } else {
                Log.e(TAG, "Command failed with exit code $exitCode")
            }


            // 4. 返回输出
            return Pair(stdout, stderr)
        } catch (e: Exception) {
            Log.e(TAG, "Shell execution failed: ${e.message}")
            return Pair("", e.message ?: "Unknown Error")
        }
//        }
    }

    // --- 实现 AIDL 接口定义的方法 ---

    override fun openApp(packageName: String) {
        // 使用 'monkey' 命令来启动应用，这是一种简单可靠的方式
//        scope.launch {
        val (out, err) = execShell("monkey -p $packageName 1")
        Log.i(TAG, "stdout=$out, stderr=$err")
//        }

    }

    override fun getUiXml(): String {
        val dumpFile = "/data/local/tmp/ui.xml"
        // 1. 先 dump 布局
        execShell("uiautomator dump $dumpFile")
        // 2. 再读取 dump 出来的文件内容并返回
        val (xml, error) = execShell("cat $dumpFile")
        if (error.isNotEmpty()) {
            Log.e(TAG, "Failed to read UI XML: $error")
            return "Error: $error"
        }
        return xml
    }

    override fun tap(x: Int, y: Int) {
//        scope.launch {
        execShell("input tap $x $y")
//        }
    }

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Int) {
//        scope.launch {
        execShell("input swipe $x1 $y1 $x2 $y2 $duration")
//        }

    }

    override fun back() {
        // 4 是 KEYCODE_BACK 的值
//        scope.launch {
        execShell("input keyevent 4")
//        }
    }

    override fun exit() {
        Log.d(TAG, "UserService exit() called. Process will terminate.")
        // 优雅地关闭这个高权限进程
        exitProcess(0)
    }

    /**
     * 辅助函数：解析 XML 并查找节点
     * @param xmlString 从 Shizuku service 获取的 UI XML
     * @param text 要查找的 text 属性
     * @param contentDesc 要查找的 content-desc 属性
     * @return 节点的 bounds (Rect)，如果没找到则返回 null
     */
    override fun findNodeBounds(xmlString: String, text: String, contentDesc: String): Rect? {
        try {
            val parser = Xml.newPullParser()
            parser.setInput(StringReader(xmlString))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "node") {
                    val nodeText = parser.getAttributeValue(null, "text")
                    val nodeDesc = parser.getAttributeValue(null, "content-desc")
                    val nodeBounds = parser.getAttributeValue(null, "bounds")

                    if ((nodeText == text || nodeDesc == contentDesc) && nodeBounds != null) {
                        // 找到了！解析 bounds="[x1,y1][x2,y2]"
                        val rect = parseBounds(nodeBounds)
                        if (rect != null) {
                            Log.i(ScriptUtils.TAG, "Found node at $nodeBounds")
                            return rect
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(ScriptUtils.TAG, "XML parsing failed", e)
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
                        Log.i(TAG, "Found node at $nodeBounds")
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
            Log.i(TAG, "Found node at $bounds")
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
            // ...
        }
        return null
    }


}