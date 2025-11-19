package auto.script.activity


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import auto.script.R
import auto.script.fragment.CloudMusicFragment
import auto.script.fragment.TaobaoFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 默认加载 TaobaoFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, TaobaoFragment())
            .commit()


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_taobao -> TaobaoFragment()
                R.id.nav_cloudmusic -> CloudMusicFragment()
                else -> null
            }
            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
            }
            true
        }


//
//        val tbStartButton: Button = findViewById(R.id.cloudmusic_start_service)
//
//        tbStartButton.setOnClickListener {
//            if (isAccessibilityServiceEnabled()) {
//
//                // 1. 使用 startService 直接命令无障碍服务启动任务
//                val serviceIntent = cloudmusic.getStartIntent(this)
//                startService(serviceIntent) // <-- 替代了 sendBroadcast
//
//                // 2. 给出反馈
//                Toast.makeText(this, "自动化脚本启动信号已发送！", Toast.LENGTH_LONG).show()
//                Log.i(TAG, "已发送启动脚本的 startService 命令。")
//
//            } else {
//                openAccessibilitySettings()
//            }
//        }
//
//        val tbStopButton: Button = findViewById(R.id.cloudmusic_stop_service)
//
//        tbStopButton.setOnClickListener {
//            if (isAccessibilityServiceEnabled()) {
//
//                // 1. 使用 startService 直接命令无障碍服务启动任务
//                val serviceIntent = cloudmusic.getStopIntent(this)
//                startService(serviceIntent) // <-- 替代了 sendBroadcast
//
//                // 2. 给出反馈
//                Toast.makeText(this, "自动化脚本停止信号已发送！", Toast.LENGTH_LONG).show()
//                Log.i(TAG, "已发送停止脚本的 startService 命令。")
//
//            } else {
//                openAccessibilitySettings()
//            }
//        }
    }


}