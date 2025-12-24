package auto.script.shizuku

import NodeResult


interface IShizukuTool {

    fun openAppByPackageName(packageName: String)

    fun openAppByActivityName(activityName: String)

    fun getUiXml(filename: String)

    fun tap(x: Int, y: Int)

    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Int)

    fun back()

    fun exit()

    fun screencap(path: String)

    fun findNodeById(resId: String): NodeResult.ShizukuNode?
    fun findNodeByText(text: String): NodeResult.ShizukuNode?
}