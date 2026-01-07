package com.yike.jarvis.nodetool

import android.os.Handler
import android.os.Looper
import javax.inject.Inject

class NodeTool @Inject constructor() {

     private val handler = Handler(Looper.getMainLooper())

     fun findByResourceId(id: String): NodePromise {
        return NodePromise(handler, IdFinder( id))
    }

     fun findByText(text: String, containerId: String? = null): NodePromise {
        return NodePromise(handler, TextFinder( text))
    }

}