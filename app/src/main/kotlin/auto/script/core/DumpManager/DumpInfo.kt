package auto.script.core.DumpManager

import auto.script.state.NeteaseState


data class DumpInfo(
    val timestamp: Long,
    val state: NeteaseState,
    val reason: FailReason,
    val message: String? = null
)
