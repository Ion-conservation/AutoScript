package auto.script.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import auto.script.utils.LogSharer
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var logFiles: List<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 简单创建一个 ListView，不需要额外的 XML 布局文件
        listView = ListView(this)
        setContentView(listView)
        title = "选择日志文件分享"

        loadAndShowFiles()
    }

    private fun loadAndShowFiles() {
        // 1. 获取日志目录
        val logDir = File(getExternalFilesDir(null), "logs")
        if (!logDir.exists()) {
            Toast.makeText(this, "暂无日志文件", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. 获取所有 .txt 文件并按最后修改时间倒序排列 (最新的在上面)
        logFiles = logDir.listFiles { _, name -> name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        if (logFiles.isEmpty()) {
            Toast.makeText(this, "暂无日志文件", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. 构建显示的数据 (文件名 + 大小)
        val displayList = logFiles.map { file ->
            val sizeKb = file.length() / 1024
            val lastMod =
                SimpleDateFormat("MM-dd HH:mm", Locale.US).format(Date(file.lastModified()))
            "${file.name}\n($lastMod, ${sizeKb}KB)"
        }

        // 4. 设置适配器
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        listView.adapter = adapter

        // 5. 设置点击事件：点击分享
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFile = logFiles[position]
            showActionDialog(selectedFile)
        }
    }

    // 增加一个确认弹窗，防止误触，也可以加入删除功能
    private fun showActionDialog(file: File) {
        AlertDialog.Builder(this)
            .setTitle(file.name)
            .setItems(arrayOf("分享到微信", "删除文件")) { _, which ->
                when (which) {
                    0 -> LogSharer.shareSpecificFile(this, file)
                    1 -> {
                        file.delete()
                        loadAndShowFiles() // 刷新列表
                        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }
}