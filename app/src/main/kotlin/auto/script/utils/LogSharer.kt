package auto.script.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

object LogSharer {

    /**
     * 分享当前日志文件到微信
     */
    fun shareLogToWeChat(context: Context) {
        val logFile = ScriptLogger.getCurrentFile()

        if (logFile == null || !logFile.exists()) {
            ScriptLogger.e("LogSharer", "日志文件不存在，无法分享")
            return
        }

        // 建议在协程中先 flush 一下，确保最新日志已写入
        GlobalScope.launch(Dispatchers.Main) {
            ScriptLogger.flush() // 简单等待写入完成

            try {
                shareFile(context, logFile)
            } catch (e: Exception) {
                ScriptLogger.e("LogSharer", "分享失败", e)
                Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareFile(context: Context, file: File) {
        // 1. 获取 FileProvider Uri
        // 注意：这里的 authority 必须与 Manifest 中的 android:authorities 保持一致
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        // 2. 构建 Intent
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // 设置文件类型
            putExtra(Intent.EXTRA_STREAM, contentUri)

            // 授予临时读取权限 (关键)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // 因为是在 Service 中启动 Activity，必须加这个标记
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // 指定包名直接跳转微信 (跳过系统选择器)
            setPackage("com.tencent.mm")
        }

        // 3. 启动分享
        try {
            context.startActivity(intent)
            ScriptLogger.i("LogSharer", "正在拉起微信分享日志...")
        } catch (e: Exception) {
            // 如果指定微信失败(例如未安装)，尝试通用分享
            ScriptLogger.e("LogSharer", "拉起微信失败，尝试通用分享", e)
            intent.setPackage(null) // 清除包名限制
            context.startActivity(Intent.createChooser(intent, "分享日志").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}