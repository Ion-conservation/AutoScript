package com.yike.jarvis.feature.netease.ui

import androidx.lifecycle.ViewModel
import com.yike.jarvis.core.a11y.repository.A11yServiceRepository
import com.yike.jarvis.core.shizuku.repository.ShizukuRepository
import com.yike.jarvis.feature.netease.controller.NeteaseController
import com.yike.jarvis.feature.netease.executor.NeteaseExecutor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NeteaseViewModel @Inject constructor(
    private val controller: NeteaseController,
    private val executor: NeteaseExecutor,
    private val a11yServiceRepository: A11yServiceRepository,
    private val shizukuRepository: ShizukuRepository
) : ViewModel() {

    val onA11yConnected = a11yServiceRepository.onConnected
    val isA11yServiceReady = controller.isA11yServiceReady
    val shizukuStatus = controller.shizukuStatus
    val consoleOutput = executor.consoleOutput // 暴露控制台输出

    fun startAutomation() = controller.startAutomation()
    fun openA11ySettings() = controller.openAccessibilitySettings()
    fun initShizukuTool() = controller.initShizukuTool()

}
