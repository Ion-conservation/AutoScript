package auto.script.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import auto.script.CloudMusicService
import auto.script.R
import auto.script.ScriptReceiver
import auto.script.utils.ScriptUtils


class CloudMusicFragment : Fragment() {

    companion object {
        private const val TAG = "CloudMusicFragment" // 使用 const val 声明编译期常量
        private const val MY_PACKAGE_NAME = "auto.script"
        private const val TARGET_PACKAGE_NAME = "com.netease.cloudmusic"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局文件
        return inflater.inflate(R.layout.fragment_cloudmusic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        val intent = Intent("auto.script")

        val startButton = view.findViewById<Button>(R.id.cloudmusic_start_service)
        startButton.setOnClickListener {

            if (ScriptUtils.isAccessibilityServiceEnabled(
                    context,
                    CloudMusicService::class.java
                )
            ) {

//                intent.putExtra("scriptName", "start_script")
//                requireContext().sendBroadcast(intent)
                // 重点：创建一个指向 ScriptReceiver 的显式 Intent
                val explicitIntent = Intent(context, ScriptReceiver::class.java)
                explicitIntent.action = "auto.script" // action 仍然可以保留
                explicitIntent.putExtra("scriptName", "start_script")

                requireContext().sendBroadcast(explicitIntent) // 发送显式广播

                ScriptUtils.logAndToast(context, TAG, "正在启动脚本。")

            } else {

                ScriptUtils.logAndToast(context, TAG, "请手动打开无障碍服务。")
                ScriptUtils.openAccessibilitySettings(context)
            }
        }

        val stopButton = view.findViewById<Button>(R.id.cloudmusic_stop_service)

        stopButton.setOnClickListener {
            val explicitIntent = Intent(context, ScriptReceiver::class.java)
            explicitIntent.action = "auto.script"
            explicitIntent.putExtra("scriptName", "stop_script")

            context.sendBroadcast(explicitIntent)

            ScriptUtils.logAndToast(context, TAG, "正在停止 CloudMusic 服务。")
        }

    }
}
