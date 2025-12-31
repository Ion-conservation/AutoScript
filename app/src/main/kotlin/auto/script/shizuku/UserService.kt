package auto.script.shizuku

import android.app.IActivityManager
import android.util.Log
import auto.script.utils.ScriptLogger
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess


class UserService : IUserService.Stub() {

    private val TAG = "UserService"

    /**
     * 内部辅助函数：真正执行 Shell 命令的地方
     * 由于 UserService 运行在高权限进程中，直接使用 Runtime.exec 执行命令。
     * @return Pair(stdout, stderr)
     */
    private fun execShell(cmd: String): Pair<String, String> {
        try {
            // 1. 直接使用 Runtime 创建进程（因为已经在高权限上下文中）
            val process = Runtime.getRuntime().exec(cmd)
            Log.i(TAG, "Executing: $cmd")

            // 2. 分别读取标准输出 (stdout) 和标准错误 (stderr)
            //    我们必须同时读取两者，否则如果缓冲区满了，进程会卡住！
            val stdout = process.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderr = process.errorStream.bufferedReader().use(BufferedReader::readText)

            // 3. 等待命令执行完毕
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Log.i(TAG, "$cmd 命令执行成功。")
            } else {
                Log.i(TAG, "$cmd 命令执行失败，退出代号： $exitCode。")
            }
            // 4. 返回输出
            return Pair(stdout, stderr)
        } catch (e: Exception) {
            Log.i(TAG, "$cmd 命令执行发生错误: ${e.message}")
            return Pair("", e.message ?: "Unknown Error")
        }
    }

    // --- 实现 AIDL 接口定义的方法 ---

    override fun screencap(path: String) {
        execShell("screencap -p \"$path\"")
    }

    /**
     *  不知道 LAUNCHER Activity 的时候使用
     * */
    override fun openAppByPackageName(packageName: String) {
        execShell("monkey -p $packageName 1")
    }

    /**
     *  知道 LAUNCHER Activity 的时候使用
     * */
    override fun openAppByActivityName(activityName: String) {
        // 使用 am start 启动应用的 LAUNCHER Activity，比 monkey 更直接
        execShell("am start -n auto.script/.MainActivity")
    }

    override fun getUiXml(filename: String?): String {
        // 1. 处理文件名逻辑：如果 filename 为 null 或为空字符串，则生成时间戳
        val finalFileName = if (filename.isNullOrBlank()) {
            val formatter = SimpleDateFormat("yyyy-MM-dd___HH-mm-ss")
            formatter.format(Date())
        } else {
            filename
        }

        val dumpFile = "/data/local/tmp/u$finalFileName.xml" // 建议加上 .xml 后缀

        // 2. 执行 dump 布局
        // 提示：uiautomator dump 如果不指定路径，默认也会存放在 /sdcard/view.xml
        execShell("uiautomator dump $dumpFile")

        // 3. 读取内容
        val (xml, err) = execShell("cat $dumpFile")

        return if (err.isNotEmpty()) {
            "Error: $err"
        } else {
            // 建议：读取完后删除临时文件，防止占用手机空间
            // execShell("rm $dumpFile")
            xml
        }
    }

    // 需要先在项目中导入相关的 AIDL 定义，或者通过反射调用
    override fun getCurrentPackageName(): String? {
        return try {
            // 使用 Shizuku 提供的 Binder 包装器
            val am = IActivityManager.Stub.asInterface(
                ShizukuBinderWrapper(SystemServiceHelper.getSystemService("activity"))
            )

            // 获取当前正在运行的 1 个最近任务
            val tasks = am.getTasks(1, 0)
            if (tasks.isNotEmpty()) {
                // 直接获取包名，没有任何 2025 的干扰
                tasks[0].topActivity?.packageName
            } else {
                null
            }
        } catch (e: Exception) {
            ScriptLogger.e("Shizuku", "通过 API 获取包名失败: ${e.message}")
            null
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
        Log.i(TAG, "UserService exit() called. Process will terminate.")
        // 优雅地关闭这个高权限进程
        exitProcess(0)
    }

}