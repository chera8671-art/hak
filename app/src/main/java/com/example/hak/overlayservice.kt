package com.poolmaster.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.WindowManager

class OverlayService : Service() {

    companion object {
        var isRunning = false
        private var overlayView: OverlayView? = null
        private var windowManager: WindowManager? = null

        fun updateOverlay() {
            overlayView?.invalidate()
        }
        
        // دالة جديدة لتحديث الإعدادات بالكامل
        fun updateSettings(ballColor: Int, thickness: Float) {
            overlayView?.updateSettings(ballColor, thickness)
        }
        
        fun stopService(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotification()
        setupOverlay()
        isRunning = true
    }
    
    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pool_overlay_channel",
                "Pool Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            
            val notification = Notification.Builder(this, "pool_overlay_channel")
                .setContentTitle("🎱 مدرب البلياردو")
                .setContentText("التطبيق نشط - اسحب الكرات لتعديل المسار")
                .setSmallIcon(android.R.drawable.ic_menu_directions)
                .setOngoing(true)
                .build()
            
            startForeground(1, notification)
        }
    }
    
    private fun setupOverlay() {
        overlayView = OverlayView(this)
        
        // تطبيق الإعدادات الحالية من MainActivity
        overlayView?.updateSettings(MainActivity.lineColor, MainActivity.lineThickness)
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager?.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            overlayView?.let { windowManager?.removeView(it) }
            overlayView = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isRunning = false
    }
}
