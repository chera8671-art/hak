package com.example.hak

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.DashPathEffect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.hypot
import kotlin.math.atan2
import kotlin.math.PI

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Tracking touch for draggable line
    private var dragMode: DragMode? = null
    private var cueBallX = 300f
    private var cueBallY = 900f
    private var targetBallX = 300f
    private var targetBallY = 500f
    
    // Ball colors (8-ball pool standard colors)
    private val BALL_COLORS = listOf(
        0xFFFFFFFF.toInt(), // 0: White (cue ball)
        0xFFFFD700.toInt(), // 1: Yellow
        0xFF0000FF.toInt(), // 2: Blue
        0xFFFF0000.toInt(), // 3: Red
        0xFF800080.toInt(), // 4: Purple
        0xFFFF6600.toInt(), // 5: Orange
        0xFF008000.toInt(), // 6: Green
        0xFF800000.toInt(), // 7: Maroon
        0xFF000000.toInt(), // 8: Black
    )
    
    private var selectedBallColor = 0xFFFFD700.toInt() // Default: Yellow

    // Paints
    private val cueBallPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt() // White
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val targetBallPaint = Paint().apply {
        color = selectedBallColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val mainLinePaint = Paint().apply {
        color = selectedBallColor
        strokeWidth = MainActivity.lineThickness
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val infiniteLinePaint = Paint().apply {
        color = (selectedBallColor and 0x00FFFFFF) or 0x80000000 // Semi-transparent
        strokeWidth = MainActivity.lineThickness
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val ghostBallPaint = Paint().apply {
        color = 0x80FFFF00.toInt() // Semi-transparent yellow
        style = Paint.Style.FILL
        isAntiAlias = true
        alpha = 128
    }

    private val dottedLinePaint = Paint().apply {
        color = 0xFF00FF00.toInt() // Green dotted
        strokeWidth = MainActivity.lineThickness / 2
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
        isAntiAlias = true
    }

    private val angleTextPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 28f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private enum class DragMode {
        CUE_BALL, TARGET_BALL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Update paint styles from settings
        mainLinePaint.color = selectedBallColor
        mainLinePaint.strokeWidth = MainActivity.lineThickness
        infiniteLinePaint.strokeWidth = MainActivity.lineThickness
        infiniteLinePaint.color = (selectedBallColor and 0x00FFFFFF) or 0x60000000
        targetBallPaint.color 
