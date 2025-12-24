package auto.script.nodetool

import auto.script.A11yService.A11yServiceTool
import auto.script.shizuku.ShizukuTool

object NodeContext {
    lateinit var a11yServiceTool: A11yServiceTool
    lateinit var shizukuServiceTool: ShizukuTool

    fun init(a11yTool: A11yServiceTool, shizukuTool: ShizukuTool) {
        a11yServiceTool = a11yTool
        shizukuServiceTool = shizukuTool
    }
}
