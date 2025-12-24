package auto.script.state

enum class NeteaseState {
    WAIT_TO_LAUNCH_APP, // 空闲状态，等待应用启动
    LAUNCHING_APP, // 正在启动 APP
    WAIT_TO_OPEN_SIDE_BAR, // 通过打开抽屉来进入免费听
    WAIT_TO_CLICK_FREE_BUTTON,   // 点击 “免费听” 按钮
    WAIT_TO_LIGHT_UP_PUZZLE, // 找到 ”看视频，点亮拼图“ 按钮，并点击
    WAIT_TO_HANDLE_REWARD_WAY, // 点击广告，进入详情页
    WAIT_TO_RETURN_APP,
}