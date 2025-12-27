package auto.script.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import auto.script.ui.theme.AutoScriptAppTheme
import auto.script.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {

            val viewModel: MainViewModel = viewModel()
            val themeStyle by viewModel.currentTheme.collectAsState()

            // 应用动态主题
            AutoScriptAppTheme(themeStyle = themeStyle) {
                MyApp()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    fun shareLog() {
//        LogSharer.shareLogToWeChat(this)

        val intent = Intent(this, LogListActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Service 启动 Activity 必须加
        startActivity(intent)
    }

//    fun openLogManager() {
//        val intent = Intent(this, LogListActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Service 启动 Activity 必须加
//        startActivity(intent)
//    }


}


