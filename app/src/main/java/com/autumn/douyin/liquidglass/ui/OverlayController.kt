package com.autumn.douyin.liquidglass.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.autumn.douyin.liquidglass.ModuleLog
import com.autumn.douyin.liquidglass.nativebar.NativeBottomBar
import com.autumn.douyin.liquidglass.nativebar.NativeMessageBadgeMonitor

class OverlayController(private val nativeBar: NativeBottomBar) {
    var selectedTab by mutableIntStateOf(nativeBar.selectedIndex)
        private set
    var messageBadgeCount by mutableIntStateOf(0)
        private set

    private val messageBadgeMonitor = NativeMessageBadgeMonitor(nativeBar.messages) {
        messageBadgeCount = it
    }

    fun start() {
        messageBadgeMonitor.start()
    }

    fun stop() {
        messageBadgeMonitor.stop()
    }

    fun clickTab(index: Int) {
        selectedTab = index
        val accepted = nativeBar.clickTab(index)
        nativeBar.tabs[index].postDelayed({
            selectedTab = nativeBar.selectedIndex
        }, 240)
        ModuleLog.info { "click tab=$index accepted=$accepted" }
    }

    fun clickPlus() {
        val accepted = nativeBar.clickPlus()
        ModuleLog.info { "click plus accepted=$accepted" }
    }

    fun longClickPlus(): Boolean {
        val accepted = nativeBar.longClickPlus()
        ModuleLog.info { "long click plus accepted=$accepted" }
        return accepted
    }

}
