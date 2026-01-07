package com.yike.jarvis.nodetool

import NodeResult
import android.view.accessibility.AccessibilityNodeInfo

abstract class NodeFinder {
    private var a11yFailCount = 0
    private val maxA11yRetries = 3
    
    abstract fun findByA11y(): AccessibilityNodeInfo?
    abstract fun findByShizuku(): NodeResult.ShizukuNode?
    
    /**
     * 带重试逻辑的查找方法
     * 优先使用 A11y，失败 3 次后才使用 Shizuku
     */
    fun findWithRetry(): NodeResult? {
        // 先尝试使用 A11y 查找
        findByA11y()?.let { 
            a11yFailCount = 0 // 成功后重置计数
            return NodeResult.A11yNode(it) 
        }
        
        // A11y 失败，增加计数
        a11yFailCount++
        
        // 只有在 A11y 失败 3 次后才使用 Shizuku
        if (a11yFailCount >= maxA11yRetries) {
            findByShizuku()?.let { return it }
        }
        
        return null
    }
    
    /**
     * 重置失败计数（可选，用于手动重置）
     */
    fun resetFailCount() {
        a11yFailCount = 0
    }
}


class IdFinder( private val resId: String) : NodeFinder() {
    override fun findByA11y(): AccessibilityNodeInfo? {
        return NodeContext.a11yServiceTool.findNodeByReourceId( resId)
    }

    override fun findByShizuku(): NodeResult.ShizukuNode? {
        return NodeContext.shizukuServiceTool.findNodeById(resId)
    }
}

class TextFinder( private val text: String) : NodeFinder() {

    override fun findByA11y(): AccessibilityNodeInfo? {
        return NodeContext.a11yServiceTool.findNodeByText(null, text)
    }

    override fun findByShizuku(): NodeResult.ShizukuNode? {
        return  NodeContext.shizukuServiceTool.findNodeByText(text)
    }
}


