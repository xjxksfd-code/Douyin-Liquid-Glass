package com.autumn.douyin.liquidglass.ui

import android.os.SystemClock
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
    private var pendingTabSelection: PendingTabSelection? = null

    fun start() {
        messageBadgeMonitor.start()
    }

    fun stop() {
        messageBadgeMonitor.stop()
    }

    fun clickTab(index: Int) {
        selectedTab = index
        val accepted = nativeBar.clickTab(index)
        pendingTabSelection = if (accepted) {
            PendingTabSelection(index, SystemClock.uptimeMillis())
        } else {
            null
        }
        nativeBar.tabs[index].postDelayed({
            syncSelectedTab(nativeBar.selectedIndex)
        }, 240)
        ModuleLog.info { "click tab=$index accepted=$accepted" }
    }

    /**
     * Mirrors the selection state from Douyin's hidden native bottom bar.
     *
     * Navigation gestures do not invoke [clickTab], so this is deliberately a separate
     * path from overlay clicks. A short grace period keeps the optimistic click indicator
     * from snapping back to the old native selection while Douyin processes a tap.
     */
    fun syncSelectedTab(nativeIndex: Int) {
        val pending = pendingTabSelection
        val now = SystemClock.uptimeMillis()
        if (pending != null && nativeIndex != pending.index &&
            now - pending.startedAtMs < NativeSelectionSettleTimeoutMs
        ) {
            return
        }

        pendingTabSelection = null
        if (selectedTab != nativeIndex) {
            selectedTab = nativeIndex
            ModuleLog.info { "synced selected tab=$nativeIndex" }
        }
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

    private data class PendingTabSelection(
        val index: Int,
        val startedAtMs: Long,
    )

    private companion object {
        const val NativeSelectionSettleTimeoutMs = 500L
    }

}
