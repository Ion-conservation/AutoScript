package com.yike.jarvis.utils

import NodeResult


object XmlParser {

    data class XmlNode(
        val text: String?,
        val bounds: String?,
        val id: String?
    )

    /**
     * 从 Shizuku dump 的 XML 字符串中查找包含指定文本的节点
     */
    fun findNodeByText(xml: String, target: String): NodeResult.ShizukuNode? {
        val nodes = parseNodes(xml)

        val match = nodes.firstOrNull { node ->
            node.text?.contains(target) == true
        } ?: return null

        val (x, y) = parseBounds(match.bounds ?: return null)

        return NodeResult.ShizukuNode(x, y, match.text)
    }

    fun findNodeById(xml: String, resId: String): NodeResult.ShizukuNode? {
        val nodes = parseNodes(xml)

        val match = nodes.firstOrNull { node ->
            node.id == resId
        } ?: return null

        val (x, y) = parseBounds(match.bounds ?: return null)

        return NodeResult.ShizukuNode(x, y, match.text)
    }


    /**
     * 解析所有节点（只提取 text 和 bounds）
     */
    private fun parseNodes(xml: String): List<XmlNode> {
        val regex = Regex(
            """<node[^>]*?text="(.*?)"[^>]*?resource-id="(.*?)"[^>]*?bounds="(\[.*?])""",
            RegexOption.DOT_MATCHES_ALL
        )

        return regex.findAll(xml).map { match ->
            XmlNode(
                text = match.groupValues[1].ifEmpty { null },
                id = match.groupValues[2].ifEmpty { null },
                bounds = match.groupValues[3]
            )
        }.toList()
    }


    /**
     * 将 bounds="[100,200][300,400]" 转换为中心点坐标
     */
    private fun parseBounds(bounds: String): Pair<Int, Int> {
        val regex = Regex("""\[(\d+),(\d+)]\[(\d+),(\d+)]""")
        val match = regex.find(bounds) ?: return 0 to 0

        val (x1, y1, x2, y2) = match.groupValues.drop(1).map { it.toInt() }
        val centerX = (x1 + x2) / 2
        val centerY = (y1 + y2) / 2

        return centerX to centerY
    }
}
