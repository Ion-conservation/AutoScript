package com.yike.jarvis.common

import android.R
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ScriptCountdownManager {
    // 保存目标时间戳
    var targetTimeMillis: Long? = null
    private var handler: Handler? = null
    private var countdownRunnable: Runnable? = null
    private var isCountdownStarted = false   // 防重复启动标志位

    // 保存目标时间（例如解析到的界面时间）
    fun saveTargetTime(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        targetTimeMillis = calendar.timeInMillis
    }

    // 前台倒计时检查（仅在进程存活时有效）
    fun startCountdownChecker(context: Context) {
        if (isCountdownStarted) return
        isCountdownStarted = true

        handler = Handler(Looper.getMainLooper())
        countdownRunnable = object : Runnable {
            override fun run() {
                targetTimeMillis?.let { target ->
                    val now = System.currentTimeMillis()
                    val diff = target - now
                    if (diff <= 0) {
                        showNotification(context, "脚本提醒", "时间到了，立即执行脚本！")
                        targetTimeMillis = null
                        stopCountdownChecker()
                    } else {
                        val minutesLeft = diff / 1000 / 60
                        Log.i("ScriptCountdown", "剩余 $minutesLeft 分钟")
                        handler?.postDelayed(this, 60_000)
                    }
                }
            }
        }
        handler?.post(countdownRunnable!!)
    }

    fun stopCountdownChecker() {
        countdownRunnable?.let { handler?.removeCallbacks(it) }
        countdownRunnable = null
        handler = null
        isCountdownStarted = false
    }

    // 每天中午 12 点提醒
    fun scheduleDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    // 发通知
    fun showNotification(context: Context, title: String, text: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "script_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "脚本提醒", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    // 启动后台 WorkManager 检查
    fun startBackgroundCountdown(context: Context) {
        val request = PeriodicWorkRequestBuilder<CountdownWorker>(60, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "CountdownChecker",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // 测试用（1分钟后立刻弹出），排查环境和清单是否正确
    fun scheduleTestInOneMinute(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val trigger = System.currentTimeMillis() + 60_000
        // 更激进的精确触发，便于测试
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        } catch (_: Throwable) {
            am.setExact(AlarmManager.RTC_WAKEUP, trigger, pi)
        }
    }
}
