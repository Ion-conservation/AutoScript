package com.yike.jarvis.nodetool

import com.yike.jarvis.core.a11y.tool.A11yServiceTool
import com.yike.jarvis.core.shizuku.tool.ShizukuTool

object NodeContext {
    lateinit var a11yServiceTool: A11yServiceTool
    lateinit var shizukuServiceTool: ShizukuTool

    fun init(a11yTool: A11yServiceTool, shizukuTool: ShizukuTool) {
        a11yServiceTool = a11yTool
        shizukuServiceTool = shizukuTool
    }
}
