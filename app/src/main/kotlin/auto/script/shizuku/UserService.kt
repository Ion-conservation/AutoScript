package auto.script.shizuku

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.BufferedReader
import kotlin.system.exitProcess


class UserService : IUserService.Stub() {

    private val TAG = "UserService"

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

}