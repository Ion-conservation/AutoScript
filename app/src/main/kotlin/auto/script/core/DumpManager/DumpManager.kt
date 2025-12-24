package auto.script.core.DumpManager

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import auto.script.utils.ScriptLogger
import java.io.File

object DumpManager {

    private const val TAG = "DumpManager"
    private const val DUMP_DIR_NAME = "netease_script_dump"

    // 可选：通过 DI 或初始化时注入
    @Volatile
    var emailSender: EmailSender? = null

    // 收件人邮箱
    @Volatile
    var defaultRecipientEmail: String? = null

    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * 核心入口：执行 dump（XML + PNG）并可选发送邮件
     */
    fun dump(
        dumpInfo: DumpInfo,
        rootNode: AccessibilityNodeInfo?,
        takeScreenshot: (String) -> Boolean, // 比如 { path -> shizukuTool.screencap(path); true }
        sendEmail: Boolean = true
    ) {
        try {
            val dir = prepareDumpDir()
            val fileBaseName = buildFileBaseName(dumpInfo)

            val xmlFile = File(dir, "$fileBaseName.uix")
            val pngFile = File(dir, "$fileBaseName.png")

            // 1. 保存 UI 树 XML
            if (rootNode != null) {
                saveNodeTreeAsXml(rootNode, xmlFile)
            } else {
                xmlFile.writeText("<root null=\"true\" />")
            }

            // 2. 截图
            val screenshotSuccess = takeScreenshot(pngFile.absolutePath)

            ScriptLogger.i(TAG, "UI dump 完成：${dumpInfo.reason} @ ${dumpInfo.state}")
            ScriptLogger.i(TAG, "XML: ${xmlFile.absolutePath}")
            if (screenshotSuccess) {
                ScriptLogger.i(TAG, "PNG: ${pngFile.absolutePath}")
            } else {
                ScriptLogger.e(TAG, "截图失败：${pngFile.absolutePath}")
            }

            // 3. 发送邮件（可选）
            if (sendEmail) {
                sendDumpByEmail(dumpInfo, xmlFile, if (screenshotSuccess) pngFile else null)
            }

        } catch (e: Exception) {
            ScriptLogger.e(TAG, "dump 失败：${e.message}")
        }
    }

    // ----------------- 内部工具方法 -----------------

    private fun prepareDumpDir(): File {
        // 你也可以直接用 /sdcard/..., 这里用外部存储目录更规范
        val dir = File(
            appContext.getExternalFilesDir(null),
            DUMP_DIR_NAME
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun buildFileBaseName(dumpInfo: DumpInfo): String {
        val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
        val timeStr = sdf.format(java.util.Date(dumpInfo.timestamp))

        val stateStr = dumpInfo.state.name
        val reasonStr = dumpInfo.reason.name

        // 20251223_142000__LAUNCHING_APP__NODE_NOT_FOUND
        return "${timeStr}__${stateStr}__${reasonStr}"
    }

    private fun saveNodeTreeAsXml(root: AccessibilityNodeInfo, xmlFile: File) {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="utf-8"?>""").append('\n')
        dumpNodeRecursive(root, sb, 0)
        xmlFile.writeText(sb.toString())
    }

    private fun dumpNodeRecursive(
        node: AccessibilityNodeInfo?,
        sb: StringBuilder,
        depth: Int
    ) {
        if (node == null) return

        val indent = "  ".repeat(depth)

        sb.append(indent).append("<node")
        sb.append(""" class="${node.className ?: ""}"""")
        sb.append(""" text="${escapeXml(node.text?.toString())}"""")
        sb.append(""" content-desc="${escapeXml(node.contentDescription?.toString())}"""")
        sb.append(""" resource-id="${node.viewIdResourceName ?: ""}"""")
        sb.append(""" clickable="${node.isClickable}"""")
        sb.append(""" enabled="${node.isEnabled}"""")
        sb.append(""" focused="${node.isFocused}"""")
        sb.append(""" focusable="${node.isFocusable}"""")
        sb.append(""" scrollable="${node.isScrollable}"""")
        sb.append(""" long-clickable="${node.isLongClickable}"""")
        sb.append(""" password="${node.isPassword}"""")
        sb.append(""" selected="${node.isSelected}"""")
        sb.append(">").append('\n')

        for (i in 0 until node.childCount) {
            dumpNodeRecursive(node.getChild(i), sb, depth + 1)
        }

        sb.append(indent).append("</node>").append('\n')
    }

    private fun escapeXml(input: String?): String {
        if (input == null) return ""
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun sendDumpByEmail(
        dumpInfo: DumpInfo,
        xmlFile: File,
        pngFile: File?
    ) {
        val recipient = defaultRecipientEmail
        val sender = emailSender

        if (recipient.isNullOrBlank() || sender == null) {
            ScriptLogger.e(TAG, "未配置 emailSender 或 defaultRecipientEmail，跳过发送邮件。")
            return
        }

        val subject = "[NeteaseScript Dump] ${dumpInfo.state} - ${dumpInfo.reason}"
        val body = buildString {
            append("脚本故障 Dump 信息：\n")
            append("时间：${java.util.Date(dumpInfo.timestamp)}\n")
            append("状态：${dumpInfo.state}\n")
            append("原因：${dumpInfo.reason}\n")
            if (!dumpInfo.message.isNullOrBlank()) {
                append("附加信息：${dumpInfo.message}\n")
            }
        }

        val attachments = mutableListOf<File>()
        attachments.add(xmlFile)
        if (pngFile != null && pngFile.exists()) {
            attachments.add(pngFile)
        }

        try {
            sender.sendEmail(
                to = recipient,
                subject = subject,
                body = body,
                attachments = attachments
            )
            ScriptLogger.i(TAG, "Dump 邮件发送成功：$recipient")
        } catch (e: Exception) {
            ScriptLogger.e(TAG, "发送 Dump 邮件失败：${e.message}")
        }
    }
}
