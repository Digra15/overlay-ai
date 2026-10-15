package com.overlay.ai

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.ConcurrentLinkedQueue

class AccessibilityHelper : AccessibilityService() {

    companion object {
    private var instance: AccessibilityHelper? = null
    private val queue = ConcurrentLinkedQueue<String>()

    fun getInstance() = instance

    fun queueAction(action: String) {
        queue.add(action)

        instance?.let { service ->
            android.os.Handler(
                android.os.Looper.getMainLooper()
            ).post {
                service.processQueueInternal()
            }
        }
    }
}

private fun processQueueInternal() {
    while (true) {
        val action = queue.poll() ?: break
        val root = rootInActiveWindow ?: continue

        when (action) {
            "CLICK" -> {
                root.performAction(
                    AccessibilityNodeInfo.ACTION_CLICK
                )
            }

            "INPUT:test" -> {
                performInput(root, "test")
            }
        }
    }
}

private fun performInput(
    node: AccessibilityNodeInfo?,
    text: String
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val args = android.os.Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
        }

        node?.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            args
        )
    }
}

    override fun onCreate() {
        super.onCreate()
        instance = this
        serviceInfo = AccessibilityServiceInfo().apply {
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent) {
        queueAction("CLICK")
    }

    override fun onInterrupt() {
        instance = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }
}
