package com.yike.jarvis.core.shizuku.service

enum class ShizukuRunningState {
    NOT_RUNNING,
    RUNNING
}

enum class ShizukuGrantState {
    NOT_GRANTED,
    GRANTED
}

enum class ShizukuBindState {
    NOT_BINDED,
    BINDED
}

data class ShizukuStatus(
    val running: ShizukuRunningState,
    val grant: ShizukuGrantState,
    val bind: ShizukuBindState
)