
package com.overlay.ai

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.ConcurrentLinkedQueue

class AccessibilityHelper : AccessibilityService() {

    companion object {
        private var instance: AccessibilityHelper? = null
        private val queue = ConcurrentLinkedQueue<String>()
        private val mainHandler = Handler(Looper.getMainLooper())

        fun getInstance(): AccessibilityHelper? = instance

        /**
         * Tambahkan aksi ke antrean.
         * Aksi diproses oleh instance AccessibilityService di main thread.
         */
        fun queueAction(action: String) {
            queue.offer(action)

            instance?.let { service ->
                mainHandler.post {
                    service.processQueueInternal()
                }
            }
        }

        /**
         * Dipertahankan untuk kompatibilitas dengan pemanggil lama.
         */
        fun processQueue() {
            instance?.let { service ->
                mainHandler.post {
                    service.processQueueInternal()
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags =
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }

        processQueueInternal()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Jangan memasukkan CLICK otomatis setiap ada event.
        // Proses hanya aksi yang memang sudah masuk antrean.
        processQueueInternal()
    }

    override fun onInterrupt() {
        // Service dapat diinterupsi oleh sistem.
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        super.onDestroy()
    }

    private fun processQueueInternal() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post {
                processQueueInternal()
            }
            return
        }

        while (true) {
            val action = queue.poll() ?: break

            when {
                action == "CLICK" -> performClickOnFocusedNode()

                action.startsWith("INPUT:") -> {
                    val text = action.substringAfter("INPUT:")
                    performInputOnFocusedNode(text)
                }

                else -> {
                    // Abaikan aksi yang tidak dikenal.
                }
            }
        }
    }

    private fun performClickOnFocusedNode() {
        val root = rootInActiveWindow ?: return

        val focusedNode =
            root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
                ?: root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                ?: return

        val clickTarget = if (focusedNode.isClickable) {
            focusedNode
        } else {
            focusedNode.parent?.takeIf { it.isClickable }
        }

        clickTarget?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun performInputOnFocusedNode(text: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return

        val root = rootInActiveWindow ?: return
        val focusedNode =
            root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return

        if (!focusedNode.isEditable) return

        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
        }

        focusedNode.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            args
        )
    }
}
