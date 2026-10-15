package com.overlay.ai

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onCreate() {
        super.onCreate()
        createFloatingView()
        startForeground(1, createNotification())
    }

    private fun createFloatingView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        floatingView = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_dialog_info)
            setOnTouchListener(FloatingTouchListener())
        }

        val params = WindowManager.LayoutParams(
            120, 120,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }

        windowManager?.addView(floatingView, params)
    }

    inner class FloatingTouchListener : View.OnTouchListener {
        private var initialX = 0f
        private var initialY = 0f
        private var initialTouchX = 0f
        private var initialTouchY = 0f

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x.toFloat()
                    initialY = params.y.toFloat()
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = (initialX + (event.rawX - initialTouchX)).toInt()
                    params.y = (initialY + (event.rawY - initialTouchY)).toInt()
                    windowManager?.updateViewLayout(v, params)
                }
            }
            return false
        }
    }

    override fun onDestroy() {
        floatingView?.let { windowManager?.removeView(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private val params: WindowManager.LayoutParams
        get() = floatingView?.layoutParams as WindowManager.LayoutParams

    private fun createNotification() = androidx.core.app.NotificationCompat.Builder(this, "overlay_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Overlay Active")
        .setContentText("Overlay AI is running")
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
        .build()
}
