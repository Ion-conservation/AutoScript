package auto.script.nodetool

import auto.script.core.a11y.tool.A11yServiceTool
import auto.script.core.shizuku.tool.ShizukuTool

object NodeContext {
    lateinit var a11yServiceTool: A11yServiceTool
    lateinit var shizukuServiceTool: ShizukuTool

    fun init(a11yTool: A11yServiceTool, shizukuTool: ShizukuTool) {
        a11yServiceTool = a11yTool
        shizukuServiceTool = shizukuTool
    }
}
