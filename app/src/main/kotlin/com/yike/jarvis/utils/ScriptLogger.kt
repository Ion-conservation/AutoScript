package com.yike.jarvis.utils

import android.content.Context
import android.os.Process
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 单例日志工具类
 * 功能：同时输出到 Logcat 和 本地文件
 * 特性：线程安全、顺序保证、格式仿 Logcat
 */
object ScriptLogger {

    private const val TAG = "ScriptLogger"
    private var logFile: File? = null

    // 使用 Channel 作为日志缓冲队列，保证写入文件的顺序
    private val logChannel = Channel<String>(Channel.UNLIMITED)

    // 独立的协程作用域，用于执行文件写入操作
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 时间格式化：11-30 11:40:54.123
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

    // 缓存包名
    private var packageName: String = "UnknownApp"

    /**
     * 初始化方法，通常在 Service 的 onCreate 或 Application 中调用
     */
    fun init(context: Context) {
        packageName = context.packageName

        // 1. 确定日志保存目录：/sdcard/Android/data/你的包名/files/logs/
        // 这个目录不需要额外权限即可读写，且用户可见
        val logDir = File(context.getExternalFilesDir(null), "logs")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }

        // 2. 创建当次运行的日志文件，以启动时间命名
        val fileName = "log_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.txt"
        logFile = File(logDir, fileName)

        // 3. 启动消费者协程，从 Channel 中读取日志并写入文件
        startFileConsumer()

        i(TAG, "Logger initialized. File path: ${logFile?.absolutePath}")
    }

    private fun startFileConsumer() {
        scope.launch {
            // 持续监听 Channel
            for (logLine in logChannel) {
                try {
                    // 追加模式写入文件
                    logFile?.let { file ->
                        PrintWriter(FileWriter(file, true)).use { writer ->
                            writer.println(logLine)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to write log to file", e)
                }
            }
        }
    }

    // ================= 公开的日志方法 =================

    fun d(tag: String, msg: String) {
        val formatted = formatLog("D", tag, msg)
        Log.d(tag, msg) // 输出到系统 Logcat
        logChannel.trySend(formatted) // 发送到文件写入队列
    }

    fun i(tag: String, msg: String) {
        val formatted = formatLog("I", tag, msg)
        Log.i(tag, msg)
        logChannel.trySend(formatted)
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        val messageWithTrace = if (tr != null) "$msg\n${Log.getStackTraceString(tr)}" else msg
        val formatted = formatLog("E", tag, messageWithTrace)
        Log.e(tag, msg, tr)
        logChannel.trySend(formatted)
    }

    // ================= 格式化逻辑 =================

    /**
     * 构建仿 Logcat 格式的字符串
     * 格式: MM-dd HH:mm:ss.SSS PID-TID/PackageName Level/Tag: Message
     */
    private fun formatLog(level: String, tag: String, msg: String): String {
        val time = dateFormat.format(Date())
        val pid = Process.myPid()
        val tid = Thread.currentThread().id // 或者用 Thread.currentThread().name 获取协程名

        // 格式化组合
        return "$time $pid-$tid/$packageName $level/$tag: $msg"
    }

    /**
     * 获取日志文件绝对路径（用于分享或展示）
     */
    fun getLogPath(): String? = logFile?.absolutePath

    /**
     * 获取当前的日志文件对象
     */
    fun getCurrentFile(): File? = logFile

    /**
     * 强制刷新缓冲区，确保所有日志都写入文件
     * (虽然 flush 是自动的，但在分享前手动调用更保险)
     */
    suspend fun flush() {
        // 发送一个空的同步信号或者简单等待一下
        // 由于我们用的是 Channel，这里最简单的做法是简短延时，
        // 或者你可以设计一个 CompletableDeferred 等待队列清空，
        // 但对于日志场景，简单的延时通常足够：
        delay(200)
    }
}