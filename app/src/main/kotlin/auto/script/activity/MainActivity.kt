package auto.script.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
                MyApp()
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


