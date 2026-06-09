package com.example.hak

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.math.PI

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val BALL_RADIUS = 35f
        private const val TOUCH_TOLERANCE = 60f
    }

    // Tracking touch for draggable elements
    private var dragMode: DragMode? = null
    
    // Ball positions
    private var cueBallX = 300f
    private var cueBallY = 900f
    private var targetBallX = 300f
    private var targetBallY = 500f
    
    // Ball colors (8-ball pool standard colors)
    @Suppress("unused")
    private val BALL_COLORS = listOf(
        Color.WHITE,      // 0: White (cue ball)
        Color.YELLOW,     // 1: Yellow
        Color.BLUE,       // 2: Blue
        Color.RED,        // 3: Red
        Color.MAGENTA,    // 4: Purple
        0xFFFF6600.toInt(), // 5: Orange
        Color.GREEN,      // 6: Green
        0xFF800000.toInt(), // 7: Maroon
        Color.BLACK       // 8: Black
    )
    
    private var selectedBallColor = Color.YELLOW // Default: Yellow

    // Paints
    private val cueBallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private lateinit var targetBallPaint: Paint
    private lateinit var mainLinePaint: Paint
    private lateinit var infiniteLinePaint: Paint
    private lateinit var ghostBallPaint: Paint
    private lateinit var dottedLinePaint: Paint
    private lateinit var angleTextPaint: Paint

    init {
        initPaints()
    }

    private fun initPaints() {
        targetBallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = selectedBallColor
            style = Paint.Style.FILL
        }

        mainLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = selectedBallColor
            strokeWidth = MainActivity.lineThickness
            style = Paint.Style.STROKE
        }

        // تصحيح: طريقة أفضل لإنشاء لون شفاف من اللون الأساسي
        infiniteLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = MainActivity.lineThickness
            style = Paint.Style.STROKE
        }
        updateInfiniteLineColor()

        ghostBallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(128, 255, 255, 0) // Semi-transparent yellow
            style = Paint.Style.FILL
        }

        dottedLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GREEN
            strokeWidth = MainActivity.lineThickness / 2
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
        }

        angleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
    }

    private fun updateInfiniteLineColor() {
        // إنشاء لون شفاف (60% شفافية) من اللون المختار
        val red = Color.red(selectedBallColor)
        val green = Color.green(selectedBallColor)
        val blue = Color.blue(selectedBallColor)
        infiniteLinePaint.color = Color.argb(96, red, green, blue) // 96 = ~38% opacity
    }

    fun setSelectedBallColor(color: Int) {
        selectedBallColor = color
        targetBallPaint.color = color
        mainLinePaint.color = color
        updateInfiniteLineColor()
        invalidate()
    }

    fun setLineThickness(thickness: Float) {
        mainLinePaint.strokeWidth = thickness
        infiniteLinePaint.strokeWidth = thickness
        dottedLinePaint.strokeWidth = thickness / 2
        invalidate()
    }

    private enum class DragMode {
        CUE_BALL, TARGET_BALL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // تحديث إعدادات الخط من MainActivity (في حال تغيرت)
        mainLinePaint.strokeWidth = MainActivity.lineThickness
        infiniteLinePaint.strokeWidth = MainActivity.lineThickness
        dottedLinePaint.strokeWidth = MainActivity.lineThickness / 2

        // حساب الزاوية من الكرة البيضاء إلى الكرة المستهدفة
        val dx = targetBallX - cueBallX
        val dy = targetBallY - cueBallY
        val angle = atan2(dy.toDouble(), dx.toDouble())

        // رسم الخط اللانهائي (من الكرة البيضاء إلى ما لا نهاية)
        drawInfiniteLine(canvas, angle)

        // رسم الخط الرئيسي (الكرة البيضاء -> الهدف)
        canvas.drawLine(cueBallX, cueBallY, targetBallX, targetBallY, mainLinePaint)

        // رسم الكرات
        canvas.drawCircle(cueBallX, cueBallY, BALL_RADIUS, cueBallPaint)
        canvas.drawCircle(targetBallX, targetBallY, BALL_RADIUS, targetBallPaint)

        // رسم الكرة الشبحية (موضع الاصطدام المتوقع)
        drawGhostBall(canvas, angle)

        // تصحيح: رسم خط الانعكاس بشكل صحيح (عمودي على خط الاصطدام)
        drawReflectionLine(canvas, angle)

        // رسم زاوية الخط
        drawAngleText(canvas, angle)
    }

    private fun drawInfiniteLine(canvas: Canvas, angle: Double) {
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()
        val maxDistance = maxOf(screenWidth, screenHeight) * 2
        
        val lineEndX = cueBallX + maxDistance * cos(angle).toFloat()
        val lineEndY = cueBallY + maxDistance * sin(angle).toFloat()
        
        canvas.drawLine(cueBallX, cueBallY, lineEndX, lineEndY, infiniteLinePaint)
    }

    private fun drawGhostBall(canvas: Canvas, angle: Double) {
        val ghostBallX = targetBallX - (BALL_RADIUS * 2) * cos(angle).toFloat()
        val ghostBallY = targetBallY - (BALL_RADIUS * 2) * sin(angle).toFloat()
        canvas.drawCircle(ghostBallX, ghostBallY, BALL_RADIUS, ghostBallPaint)
    }

    private fun drawReflectionLine(canvas: Canvas, angle: Double) {
        // تصحيح: خط الانعكاس يكون عمودياً على مسار الكرة بعد الاصطدام
        // نرسم خطاً متقطعاً عمودياً على نقطة التلامس
        val perpendicularAngle = angle + PI / 2
        
        // نقطة التلامس تكون على بعد نصف قطر من مركز الكرة المستهدفة
        val contactX = targetBallX - BALL_RADIUS * cos(angle).toFloat()
        val contactY = targetBallY - BALL_RADIUS * sin(angle).toFloat()
        
        val reflectionEndX = contactX + 150f * cos(perpendicularAngle).toFloat()
        val reflectionEndY = contactY + 150f * sin(perpendicularAngle).toFloat()
        
        canvas.drawLine(contactX, contactY, reflectionEndX, reflectionEndY, dottedLinePaint)
        
        // رسم الجانب الآخر للانعكاس
        val reflectionEndX2 = contactX - 150f * cos(perpendicularAngle).toFloat()
        val reflectionEndY2 = contactY - 150f * sin(perpendicularAngle).toFloat()
        canvas.drawLine(contactX, contactY, reflectionEndX2, reflectionEndY2, dottedLinePaint)
    }

    private fun drawAngleText(canvas: Canvas, angle: Double) {
        val angleDegrees = Math.toDegrees(angle)
        val text = String.format("%.1f°", angleDegrees)
        canvas.drawText(text, targetBallX, targetBallY - BALL_RADIUS - 15f, angleTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragMode = when {
                    distance(x, y, cueBallX, cueBallY) < TOUCH_TOLERANCE -> DragMode.CUE_BALL
                    distance(x, y, targetBallX, targetBallY) < TOUCH_TOLERANCE -> DragMode.TARGET_BALL
                    else -> null
                }
                return dragMode != null
            }
            
            MotionEvent.ACTION_MOVE -> {
                when (dragMode) {
                    DragMode.CUE_BALL -> {
                        cueBallX = x.coerceIn(BALL_RADIUS, width - BALL_RADIUS)
                        cueBallY = y.coerceIn(BALL_RADIUS, height - BALL_RADIUS)
                        invalidate()
                    }
                    DragMode.TARGET_BALL -> {
                        targetBallX = x.coerceIn(BALL_RADIUS, width - BALL_RADIUS)
                        targetBallY = y.coerceIn(BALL_RADIUS, height - BALL_RADIUS)
                        invalidate()
                    }
                    null -> return false
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dragMode = null
            }
        }
        return true
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return hypot((x1 - x2).toDouble(), (y1 - y2).toDouble()).toFloat()
    }
}
