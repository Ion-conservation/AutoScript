package auto.script.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import auto.script.R


class TaobaoFragment : Fragment() {

    companion object {
        private const val TAG = "CloudMusicFragment" // 使用 const val 声明编译期常量
        private const val MY_PACKAGE_NAME = "auto.script"
        private const val TARGET_PACKAGE_NAME = "com.taobao.taobao"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局文件
        return inflater.inflate(R.layout.fragment_taobao, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 在这里绑定按钮事件
        val startButton = view.findViewById<Button>(R.id.tb_start_service)
        startButton.setOnClickListener {
            Toast.makeText(requireContext(), "启动 Taobao 服务", Toast.LENGTH_SHORT).show()
        }

        val stopButton = view.findViewById<Button>(R.id.tb_stop_service)
        stopButton.setOnClickListener {
            Toast.makeText(requireContext(), "停止 Taobao 服务", Toast.LENGTH_SHORT).show()
        }
    }
}
