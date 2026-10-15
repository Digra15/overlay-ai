package com.overlay.ai

import android.app.Application
import android.content.Context
import android.provider.Settings

class OverlayApp : Application() {
    companion object {
        lateinit var instance: OverlayApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun hasOverlayPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    fun hasAccessibilityEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        return am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC)
            .any { it.id.contains(packageName) }
    }
}
