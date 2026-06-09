package com.poolmaster.overlay

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        var lineColor: Int = 0xFFFFD700.toInt() // اللون الأصفر (متطابق مع OverlayView)
        var lineThickness: Float = 6f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // زر منح الإذن
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

        // زر تشغيل/إيقاف الـ Overlay
        findViewById<Button>(R.id.btnToggleOverlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "⚠️ امنح الإذن أولاً!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, OverlayService::class.java)
            if (OverlayService.isRunning) {
                // إيقاف الخدمة
                stopService(intent)
                findViewById<Button>(R.id.btnToggleOverlay).text = "▶️ تشغيل Overlay"
                Toast.makeText(this, "❌ تم إيقاف Overlay", Toast.LENGTH_SHORT).show()
            } else {
                // تشغيل الخدمة
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                findViewById<Button>(R.id.btnToggleOverlay).text = "⏹️ إيقاف Overlay"
                Toast.makeText(this, "✅ تم تشغيل Overlay!", Toast.LENGTH_SHORT).show()
            }
        }

        // أزرار تغيير اللون
        findViewById<Button>(R.id.btnColorRed).setOnClickListener {
            lineColor = 0xFFFF0000.toInt()
            updateOverlaySettings()
            Toast.makeText(this, "🔴 اللون الأحمر", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnColorGreen).setOnClickListener {
            lineColor = 0xFF00FF00.toInt()
            updateOverlaySettings()
            Toast.makeText(this, "🟢 اللون الأخضر", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnColorBlue).setOnClickListener {
            lineColor = 0xFF0099FF.toInt()
            updateOverlaySettings()
            Toast.makeText(this, "🔵 اللون الأزرق", Toast.LENGTH_SHORT).show()
        }

        // SeekBar لتحديد السماكة
        val seekBar = findViewById<SeekBar>(R.id.seekBarThickness)
        seekBar.apply {
            max = 20
            progress = lineThickness.toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        lineThickness = progress.toFloat()
                        updateOverlaySettings()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun updateOverlaySettings() {
        if (OverlayService.isRunning) {
            OverlayService.updateSettings(lineColor, lineThickness)
        }
    }
}
