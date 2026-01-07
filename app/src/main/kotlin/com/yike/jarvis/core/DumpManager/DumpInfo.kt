package com.yike.jarvis.core.DumpManager

import com.yike.jarvis.feature.netease.common.NeteaseState


data class DumpInfo(
    val timestamp: Long,
    val state: NeteaseState,
    val reason: FailReason,
    val message: String? = null
)
