package com.poolmaster.overlay

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast

class MainActivity : Activity() {

    companion object {
        var lineColor: Int = 0xFF00FFFF.toInt() // Cyan default
        var lineThickness: Float = 4f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Grant Permission Button
        findViewById<Button>(R.id.btnGrantPermission).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "✅ الإذن ممنوح مسبقاً!", Toast.LENGTH_SHORT).show()
            }
        }

        // Toggle Overlay Button
        findViewById<Button>(R.id.btnToggleOverlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "⚠️ امنح الإذن أولاً!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, OverlayService::class.java)
            if (OverlayService.isRunning) {
                // Stop service
                stopService(intent)
                findViewById<Button>(R.id.btnToggleOverlay).text = "▶️ تشغيل Overlay"
                Toast.makeText(this, "❌ تم إيقاف Overlay", Toast.LENGTH_SHORT).show()
            } else {
                // Start service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                findViewById<Button>(R.id.btnToggleOverlay).text = "⏹️ إيقاف Overlay"
                Toast.makeText(this, "✅ تم تشغيل Overlay!", Toast.LENGTH_SHORT).show()
            }
        }

        // Color Buttons
        findViewById<Button>(R.id.btnColorRed).setOnClickListener {
            MainActivity.lineColor = 0xFFFF0000.toInt()
            OverlayService.updateOverlay()
            Toast.makeText(this, "🔴 اللون الأحمر", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnColorGreen).setOnClickListener {
            MainActivity.lineColor = 0xFF00FF00.toInt()
            OverlayService.updateOverlay()
            Toast.makeText(this, "🟢 اللون الأخضر", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnColorBlue).setOnClickListener {
            MainActivity.lineColor = 0xFF00BFFF.toInt()
            OverlayService.updateOverlay()
            Toast.makeText(this, "🔵 اللون الأزرق", Toast.LENGTH_SHORT).show()
        }

        // Thickness SeekBar
        findViewById<SeekBar>(R.id.seekBarThickness).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                MainActivity.lineThickness = progress.toFloat()
                OverlayService.updateOverlay()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
