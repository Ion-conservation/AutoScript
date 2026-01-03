package auto.script.feature.netease

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import auto.script.BuildConfig
import auto.script.activity.MainActivity
import rikka.shizuku.Shizuku


/**
 * 依赖路径
 * UI → NeteaseViewModel → NeteaseController → Repository / Executor
 * */

@Composable
fun Netease(navController: NavController, viewModel: NeteaseViewModel = hiltViewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val appContext = LocalContext.current.applicationContext

    LaunchedEffect(Unit) {
//        viewModel.initShizukuTool()
        Log.e("APP", "BuildConfig.APPLICATION_ID = ${BuildConfig.APPLICATION_ID}")

        Shizuku.addBinderReceivedListener {
            Log.e("ShizukuVM", "binderReceived")
        }

        Shizuku.addRequestPermissionResultListener { requestCode, grantResult ->
            Log.e("ShizukuVM", "permissionResult = $grantResult")
        }


    }
    LaunchedEffect(Unit) {

        viewModel.onA11yConnected.collect {
            val intent =
                Intent(appContext, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
            val pendingIntent = PendingIntent.getActivity(
                appContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent.send()
        }
    }


    val isA11yServiceReady by viewModel.isA11yServiceReady.collectAsState()
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()
    val consoleOutput by viewModel.consoleOutput.collectAsState()

    NeteaseScreen(
        isA11yServiceReady = isA11yServiceReady,
        shizukuStatus = shizukuStatus,
        consoleOutput = consoleOutput,
        openA11ySettings = { viewModel.openA11ySettings() },
        initShizukuTool = { viewModel.initShizukuTool() },
        onStart = { viewModel.startAutomation() }
    )
}

