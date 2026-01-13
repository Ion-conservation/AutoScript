package com.yike.jarvis.core.shizuku.tool

import NodeResult
import com.yike.jarvis.core.shizuku.repository.ShizukuRepository
import com.yike.jarvis.nodetool.NodeContext
import com.yike.jarvis.utils.ScriptLogger
import com.yike.jarvis.utils.XmlParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShizukuTool @Inject constructor(
    private val shizukuRepository: ShizukuRepository
) : IShizukuTool {

    private val TAG = "ShizukuTool"

    init {
        NodeContext.shizukuServiceTool = this
    }

    override fun openAppByPackageName(packageName: String) {
        shizukuRepository.withService { shizukuService ->
            shizukuService.openAppByPackageName(packageName)
        }
    }

    override fun openAppByActivityName(activityName: String) {
        shizukuRepository.withService { shizukuService ->
            shizukuService.openAppByActivityName(activityName)
        }
    }

    override fun getUiXml(filename: String) {
        shizukuRepository.withService { shizukuService ->
            shizukuService.getUiXml(filename)
        }
    }

    override fun tap(x: Int, y: Int) {
        shizukuRepository.withService { shizukuService ->
            ScriptLogger.i("ShizukuTool", "tap: $x, $y")
            shizukuService.tap(x, y)
        }
    }

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Int) {
        shizukuRepository.withService { shizukuService ->
            shizukuService.swipe(x1, y1, x2, y2, duration)
        }
    }

    override fun back() {
        shizukuRepository.withService { shizukuService ->
            shizukuService.back()
        }
    }

    override fun exit() {
        shizukuRepository.withService { shizukuService ->
            shizukuService.exit()
        }
    }


    override fun screencap(path: String) {
        shizukuRepository.withService { shizukuService ->
            shizukuService.screencap(path)
        }
    }

    override fun findNodeById(resId: String): NodeResult.ShizukuNode? {
        return shizukuRepository.withService { shizukuService ->
            val xml = shizukuService.getUiXml(null) ?: return@withService null
            XmlParser.findNodeById(xml, resId)
        }
    }

    override fun findNodeByText(text: String): NodeResult.ShizukuNode? {
        return shizukuRepository.withService { shizukuService ->
            val xml = shizukuService.getUiXml(null) ?: return@withService null
            XmlParser.findNodeByText(xml, text)
        }
    }

    override fun getCurrentPackageName(): String? {
        return shizukuRepository.withService { shizukuService ->
            shizukuService.getCurrentPackageName()
        }
    }


}