package auto.script.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * 通用状态栏通知服务
 * 提供统一的通知发送接口，用于应用各模块需要发送通知时使用
 */
object NotificationService {
    
    // 通知渠道 ID 常量定义
    private const val CHANNEL_ID_DEFAULT = "auto_script_default"
    private const val CHANNEL_ID_TASK = "auto_script_task"
    private const val CHANNEL_ID_ERROR = "auto_script_error"
    
    // 通知 ID 常量定义
    private const val NOTIFICATION_ID_DEFAULT = 1001
    private const val NOTIFICATION_ID_TASK = 1002
    private const val NOTIFICATION_ID_ERROR = 1003
    
    /**
     * 通知等级枚举
     */
    enum class NotificationLevel {
        LOW, NORMAL, HIGH
    }
    
    /**
     * 通知类型枚举
     */
    enum class NotificationType(
        val channelId: String,
        val channelName: String,
        val notificationId: Int,
        val level: NotificationLevel
    ) {
        DEFAULT(
            channelId = CHANNEL_ID_DEFAULT,
            channelName = "默认通知",
            notificationId = NOTIFICATION_ID_DEFAULT,
            level = NotificationLevel.NORMAL
        ),
        TASK(
            channelId = CHANNEL_ID_TASK,
            channelName = "任务提醒",
            notificationId = NOTIFICATION_ID_TASK,
            level = NotificationLevel.HIGH
        ),
        ERROR(
            channelId = CHANNEL_ID_ERROR,
            channelName = "错误提醒",
            notificationId = NOTIFICATION_ID_ERROR,
            level = NotificationLevel.HIGH
        )
    }
    
    /**
     * 发送通知的通用接口
     *
     * @param context 应用上下文
     * @param title 通知标题
     * @param text 通知内容
     * @param type 通知类型，默认为 DEFAULT
     * @param largeText 可选的长文本内容
     * @param autoCancel 是否在点击后自动取消通知，默认 true
     */
    fun showNotification(
        context: Context,
        title: String,
        text: String,
        type: NotificationType = NotificationType.DEFAULT,
        largeText: String? = null,
        autoCancel: Boolean = true
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // 创建或更新通知渠道（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = when (type.level) {
                NotificationLevel.LOW -> NotificationManager.IMPORTANCE_LOW
                NotificationLevel.NORMAL -> NotificationManager.IMPORTANCE_DEFAULT
                NotificationLevel.HIGH -> NotificationManager.IMPORTANCE_HIGH
            }
            val channel = NotificationChannel(type.channelId, type.channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }
        
        // 创建点击通知返回应用的 Intent
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // 构建通知
        val notificationBuilder = NotificationCompat.Builder(context, type.channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(autoCancel)
        
        // 设置通知优先级（低版本兼容性）
        notificationBuilder.priority = when (type.level) {
            NotificationLevel.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationLevel.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
            NotificationLevel.HIGH -> NotificationCompat.PRIORITY_HIGH
        }
        
        // 添加长文本（如果提供）
        if (!largeText.isNullOrEmpty()) {
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle().bigText(largeText)
            )
        }
        
        notificationManager.notify(type.notificationId, notificationBuilder.build())
    }
    
    /**
     * 发送任务提醒通知（便捷方法）
     *
     * @param context 应用上下文
     * @param taskName 任务名称
     * @param message 提醒消息
     */
    fun showTaskNotification(
        context: Context,
        taskName: String,
        message: String = "时间到了，立即执行任务！"
    ) {
        showNotification(
            context = context,
            title = taskName,
            text = message,
            type = NotificationType.TASK,
            autoCancel = true
        )
    }
    
    /**
     * 发送错误通知（便捷方法）
     *
     * @param context 应用上下文
     * @param errorTitle 错误标题
     * @param errorMessage 错误消息
     */
    fun showErrorNotification(
        context: Context,
        errorTitle: String,
        errorMessage: String
    ) {
        showNotification(
            context = context,
            title = errorTitle,
            text = errorMessage,
            type = NotificationType.ERROR,
            autoCancel = true
        )
    }
    
    /**
     * 取消指定类型的通知
     *
     * @param context 应用上下文
     * @param type 通知类型
     */
    fun cancelNotification(context: Context, type: NotificationType) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(type.notificationId)
    }
    
    /**
     * 取消所有通知
     *
     * @param context 应用上下文
     */
    fun cancelAllNotifications(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}
