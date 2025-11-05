package auto.script.utils

import android.view.accessibility.AccessibilityNodeInfo

object FindEle {

    fun findByText(
        root: AccessibilityNodeInfo?,
        text: String,
        clickable: Boolean = true,
        returnParent: Boolean = false
    ): AccessibilityNodeInfo? {
        val nodes = root?.findAccessibilityNodeInfosByText(text) ?: return null
        return nodes.firstOrNull { node ->
            when {
                clickable && node.isClickable -> true
                clickable && returnParent -> findClickableParent(node) != null
                !clickable -> true
                else -> false
            }
        }?.let { node ->
            if (clickable && returnParent) findClickableParent(node) ?: node else node
        }
    }

    fun findByResourceId(
        root: AccessibilityNodeInfo?,
        resourceId: String,
        clickable: Boolean = true,
        returnParent: Boolean = false
    ): AccessibilityNodeInfo? {
        val nodes = root?.findAccessibilityNodeInfosByViewId(resourceId) ?: return null
        return nodes.firstOrNull { node ->
            when {
                clickable && node.isClickable -> true
                clickable && returnParent -> findClickableParent(node) != null
                !clickable -> true
                else -> false
            }
        }?.let { node ->
            if (clickable && returnParent) findClickableParent(node) ?: node else node
        }
    }

    fun findByClassname(
        root: AccessibilityNodeInfo?,
        className: String,
        clickable: Boolean = true,
        returnParent: Boolean = false
    ): AccessibilityNodeInfo? {
        fun search(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null

            val match = node.className == className &&
                    (!clickable || node.isClickable || (returnParent && findClickableParent(node) != null))

            if (match) {
                return if (clickable && returnParent) findClickableParent(node) ?: node else node
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                val result = search(child)
                if (result != null) return result
            }

            return null
        }

        return search(root)
    }


    fun findByAttributes(
        root: AccessibilityNodeInfo?,
        text: String? = null,
        resourceId: String? = null,
        className: String? = null,
        clickable: Boolean = true,
        returnParent: Boolean = false
    ): AccessibilityNodeInfo? {
        if (root == null) return null
        val candidates = mutableListOf<AccessibilityNodeInfo>()

        if (!text.isNullOrEmpty()) {
            candidates += root.findAccessibilityNodeInfosByText(text).orEmpty()
        }
        if (!resourceId.isNullOrEmpty()) {
            candidates += root.findAccessibilityNodeInfosByViewId(resourceId).orEmpty()
        }
        if (!className.isNullOrEmpty()) {
            findByClassname(root, className)?.let { candidates += it }
        }

        val unique = candidates.distinct()

        val matched = unique.firstOrNull { node ->
            val matchText = text.isNullOrEmpty() || node.text?.toString() == text
            val matchId = resourceId.isNullOrEmpty() || node.viewIdResourceName == resourceId
            val matchClass = className.isNullOrEmpty() || node.className == className
            val matchClickable =
                !clickable || node.isClickable || (returnParent && findClickableParent(node) != null)
            matchText && matchId && matchClass && matchClickable
        }

        return if (clickable && returnParent && matched != null) {
            findClickableParent(matched) ?: matched
        } else {
            matched
        }
    }

    fun findEle(
        root: AccessibilityNodeInfo?,
        text: String? = null,
        resourceId: String? = null,
        classname: String? = null,
        clickable: Boolean = true,
        returnParent: Boolean = false
    ): AccessibilityNodeInfo? {
        return findByAttributes(root, text, resourceId, classname, clickable, returnParent)
    }

    fun findClickableTextWithParent(
        root: AccessibilityNodeInfo?,
        text: String
    ): AccessibilityNodeInfo? =
        findByText(root, text, clickable = true, returnParent = true)

    fun findClickableClassWithParent(
        root: AccessibilityNodeInfo?,
        className: String
    ): AccessibilityNodeInfo? =
        findByClassname(root, className, clickable = true, returnParent = true)

    fun findClickableResourceIdWithParent(
        root: AccessibilityNodeInfo?,
        resourceId: String
    ): AccessibilityNodeInfo? =
        findByResourceId(root, resourceId, clickable = true, returnParent = true)

    fun findClickableParent(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var current = node?.parent
        while (current != null) {
            if (current.isClickable) return current
            current = current.parent
        }
        return null
    }

    // 当 id 不带有 packagename 的时候属于 rawId
    fun findNodeByRawIdWithTextCheck(
        root: AccessibilityNodeInfo?,
        resourceId: String,
        targetTexts: List<String>
    ): AccessibilityNodeInfo? {
        fun containsTargetText(node: AccessibilityNodeInfo?): Boolean {
            if (node == null) return false
            val text = node.text?.toString() ?: ""
            if (targetTexts.any { it == text }) return true
            for (i in 0 until node.childCount) {
                if (containsTargetText(node.getChild(i))) return true
            }
            return false
        }

        fun traverse(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null
            if (node.viewIdResourceName == resourceId && containsTargetText(node)) {
                return node
            }
            for (i in 0 until node.childCount) {
                val found = traverse(node.getChild(i))
                if (found != null) return found
            }
            return null
        }

        return traverse(root)
    }
}
