package com.overlay.ai

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.overlay.ai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val OVERLAY_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.btnStart.setOnClickListener { startOverlayService() }
        binding.btnCheckStatus.setOnClickListener { checkStatus() }

        checkPermissions()
    }

    private fun checkPermissions() {
        if (!OverlayApp.instance.hasOverlayPermission()) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivityForResult(intent, OVERLAY_REQUEST_CODE)
        }

        if (!OverlayApp.instance.hasAccessibilityEnabled()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun startOverlayService() {
        startService(Intent(this, OverlayService::class.java))
    }

    private fun checkStatus() {
        val overlay = OverlayApp.instance.hasOverlayPermission()
        val accessibility = OverlayApp.instance.hasAccessibilityEnabled()
        binding.tvStatus.text = "Overlay: $overlay | Accessibility: $accessibility"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_REQUEST_CODE) {
            checkStatus()
        }
    }
}
