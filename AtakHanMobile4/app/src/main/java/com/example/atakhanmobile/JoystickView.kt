package com.example.atakhanmobile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var basePaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray)
        isAntiAlias = true
    }
    private var hatPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.holo_red_light)
        isAntiAlias = true
    }
    private var ringPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.white)
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private var centerX = 0f
    private var centerY = 0f
    private var baseRadius = 0f
    private var hatRadius = 0f

    private var hatX = 0f
    private var hatY = 0f

    private var joystickListener: JoystickListener? = null

    var isHorizontalSnapBack: Boolean = false
        set(value) {
            field = value
            // Extra logic if needed when the property is set
        }
    var isLeftJoystick: Boolean = false

    fun setJoystickListener(listener: JoystickListener) {
        joystickListener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = (w / 2).toFloat()
        centerY = (h / 2).toFloat()
        baseRadius = min(w, h) / 2.5f
        hatRadius = min(w, h) / 13f

        hatX = centerX
        hatY = centerY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the base of the joystick
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint)

        // Draw the concentric circles
        val ringStep = baseRadius / 5
        for (i in 1..4) {
            canvas.drawCircle(centerX, centerY, ringStep * i, ringPaint)
        }

        // Draw the hat of the joystick
        canvas.drawCircle(hatX, hatY, hatRadius, hatPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val displacement = sqrt((event.x - centerX).pow(2) + (event.y - centerY).pow(2))
        val ratio = if (displacement < baseRadius) 1f else baseRadius / displacement

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                hatX = centerX + (event.x - centerX) * ratio
                hatY = centerY + (event.y - centerY) * ratio
                if (isLeftJoystick) {
                    // If x displacement is less than 20% of the radius, keep the hat in the center
                    if (Math.abs(event.x - centerX) < 0.2 * baseRadius) {
                        hatX = centerX
                    }
                } else {
                    // If y displacement is less than 20% of the radius, keep the hat in the center
                    if (Math.abs(event.y - centerY) < 0.2 * baseRadius) {
                        hatY = centerY
                    }
                    if (Math.abs(event.x - centerX) < 0.2 * baseRadius) {
                        hatX = centerX
                    }
                }
                invalidate()

                joystickListener?.onJoystickMoved(
                    (hatX - centerX) / baseRadius,
                    (hatY - centerY) / baseRadius,
                    id
                )
            }
            MotionEvent.ACTION_UP -> {
                if (isHorizontalSnapBack) {
                    hatX = centerX
                }
                if (isLeftJoystick) {
                    // Reset rudder value to zero when touch is released
                    hatX = centerX
                    joystickListener?.onJoystickMoved(
                        (hatX - centerX) / baseRadius,
                        (hatY - centerY) / baseRadius,
                        id
                    )
                } else {
                    // Rudder değerini sıfırla
                    hatY = centerY
                    joystickListener?.onJoystickMoved(
                        0f,
                        (hatY - centerY) / baseRadius,
                        id
                    )
                }
                invalidate()
            }
        }
        return true
    }

}