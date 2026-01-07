package auto.script.feature.scheduler

import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import auto.script.common.NotificationService
import auto.script.feature.scheduler.ui.TaskSchedulerViewModel

/**
 * 通知系统使用示例
 *
 * 本文件演示如何在应用的不同模块中使用 NotificationService 和 TaskSchedulerViewModel
 */

// ============= 示例 1: 直接使用 NotificationService 发送通知 =============

/**
 * 使用通用通知接口
 *
 * 适用场景：需要在应用的任何地方发送通知
 */
fun exampleDirectNotification(context: Context) {
    // 发送默认通知
    NotificationService.showNotification(
        context = context,
        title = "默认通知",
        text = "这是一条默认通知",
        type = NotificationService.NotificationType.DEFAULT
    )

    // 发送任务提醒通知
    NotificationService.showTaskNotification(
        context = context,
        taskName = "我的任务",
        message = "时间到了，请执行任务！"
    )

    // 发送错误通知
    NotificationService.showErrorNotification(
        context = context,
        errorTitle = "执行错误",
        errorMessage = "任务执行过程中出现错误"
    )

    // 发送自定义内容的高级通知
    NotificationService.showNotification(
        context = context,
        title = "高级通知",
        text = "摘要内容",
        type = NotificationService.NotificationType.TASK,
        largeText = "这是一条长文本通知，可以包含更详细的信息...",
        autoCancel = true
    )

    // 取消指定类型的通知
    NotificationService.cancelNotification(
        context = context,
        type = NotificationService.NotificationType.TASK
    )

    // 取消所有通知
    NotificationService.cancelAllNotifications(context)
}

// ============= 示例 2: 在 ViewModel 中使用任务监视器 =============

/**
 * 设置 ViewModel 的上下文并启动任务监视
 *
 * 建议在 Application 或 MainActivity 中调用此函数
 */
fun setupTaskSchedulerViewModel(
    viewModel: TaskSchedulerViewModel,
    context: Context
) {
    // 注入应用上下文（用于发送通知）
    viewModel.applicationContext = context

    // 启动任务时间监视器
    // 监视器将每分钟检查一次活跃任务是否到达指定时间
    viewModel.startTaskMonitoring()
}

/**
 * 停止任务监视器
 *
 * 建议在 Activity/Fragment 销毁或应用后台时调用
 */
fun stopTaskSchedulerViewModel(viewModel: TaskSchedulerViewModel) {
    viewModel.stopTaskMonitoring()
}

// ============= 示例 3: 在 Composable 中的使用 =============

/**
 * UI 层中的通知按钮示例
 */
@Composable
fun NotificationExampleButtons(
    context: Context,
    viewModel: TaskSchedulerViewModel? = null
) {
    // 发送任务通知按钮
    Button(
        onClick = {
            NotificationService.showTaskNotification(
                context = context,
                taskName = "示例任务",
                message = "这是一个示例任务通知"
            )
        }
    ) {
        Text("发送任务通知")
    }

    // 发送错误通知按钮
    Button(
        onClick = {
            NotificationService.showErrorNotification(
                context = context,
                errorTitle = "示例错误",
                errorMessage = "这是一个示例错误通知"
            )
        }
    ) {
        Text("发送错误通知")
    }

    // 启动任务监视器按钮
    viewModel?.let {
        Button(
            onClick = {
                it.applicationContext = context
                it.startTaskMonitoring()
            }
        ) {
            Text("启动任务监视")
        }
    }

    // 停止任务监视器按钮
    viewModel?.let {
        Button(
            onClick = {
                it.stopTaskMonitoring()
            }
        ) {
            Text("停止任务监视")
        }
    }
}

// ============= 示例 4: 在 Application 中初始化 =============

/**
 * 在 Application 的 onCreate 中调用此函数来初始化通知系统
 */
fun initializeNotificationSystem(context: Context) {
    // 初始化各个通知渠道
    // NotificationService 会在首次使用时自动创建渠道
    // 但这里可以提前验证系统初始化

    // 测试通知系统是否正常
    NotificationService.showNotification(
        context = context,
        title = "应用启动",
        text = "应用已启动，通知系统已初始化",
        type = NotificationService.NotificationType.DEFAULT,
        autoCancel = true
    )
}

// ============= 示例 5: 与其他模块集成 =============

/**
 * 假设在网易云音乐模块中需要发送通知
 * 可以直接调用 NotificationService
 */
fun exampleNeteaseExecutorNotification(context: Context) {
    // 脚本执行开始通知
    NotificationService.showNotification(
        context = context,
        title = "网易云脚本",
        text = "脚本执行开始...",
        type = NotificationService.NotificationType.DEFAULT
    )

    // 脚本执行完成通知
    NotificationService.showTaskNotification(
        context = context,
        taskName = "网易云脚本",
        message = "脚本执行完成"
    )
}

// ============= 通知系统设计说明 =============

/**
 * NotificationService 的特点：
 *
 * 1. 通用接口设计
 *    - showNotification(): 通用通知方法，支持自定义所有参数
 *    - showTaskNotification(): 任务通知便捷方法
 *    - showErrorNotification(): 错误通知便捷方法
 *
 * 2. 多种通知类型
 *    - NotificationType.DEFAULT: 默认通知（中等优先级）
 *    - NotificationType.TASK: 任务提醒（高优先级）
 *    - NotificationType.ERROR: 错误提醒（高优先级）
 *
 * 3. Android 版本兼容性
 *    - 自动为 Android 8.0+ 创建通知渠道
 *    - 为低版本设置优先级参数
 *
 * 4. 点击行为
 *    - 点击通知默认回到应用主界面
 *    - autoCancel 参数控制是否点击后自动消失
 *
 * 5. TaskSchedulerViewModel 集成
 *    - startTaskMonitoring(): 启动每分钟检查一次的任务监视器
 *    - stopTaskMonitoring(): 停止任务监视器
 *    - applicationContext: 用于发送通知的应用上下文
 *    - 当任务时间到达时自动发送通知
 */
