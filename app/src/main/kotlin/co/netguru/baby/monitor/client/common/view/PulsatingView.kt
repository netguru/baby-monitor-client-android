package co.netguru.baby.monitor.client.common.view

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import co.netguru.baby.monitor.client.R

class PulsatingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val firstPulsePaint = Paint().apply {
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.STROKE
    }
    private var firstPulseAnimator: ValueAnimator? = null
    private var firstPulseSize = MIN_SIZE

    private val secondPulsePaint = Paint().apply {
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.STROKE
    }
    private var secondPulseAnimator: ValueAnimator? = null
    private var secondPulseSize = MIN_SIZE

    private var circleSize = 0
    private var circlePaint = Paint()

    private var activeColor: Int
    private var inactiveColor: Int

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.PulsatingView
        ).run {
            try {
                circleSize = getDimensionPixelSize(R.styleable.PulsatingView_circleSize, 0)
                activeColor = getColor(
                    R.styleable.PulsatingView_activeColor,
                    ContextCompat.getColor(context, R.color.active_pulsating)
                )
                inactiveColor = getColor(
                    R.styleable.PulsatingView_inactiveColor,
                    ContextCompat.getColor(context, R.color.inactive_pulsating)
                )

                firstPulsePaint.color = activeColor
                secondPulsePaint.color = activeColor
            } finally {
                recycle()
            }
        }
    }

    fun start() {
        circlePaint.color = activeColor

        val propertyRadius = PropertyValuesHolder.ofFloat(PROPERTY_SIZE, MIN_SIZE, MAX_SIZE)
        val propertyRotate = PropertyValuesHolder.ofInt(PROPERTY_OPACITY, MAX_ALPHA, MIN_ALPHA)

        firstPulseAnimator = ValueAnimator().apply {
            setValues(propertyRadius, propertyRotate)
            duration = ANIMATION_DURATION
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { valueAnimator ->
                firstPulseSize = valueAnimator.animatedValue as Float
                firstPulsePaint.alpha = valueAnimator.getAnimatedValue(PROPERTY_OPACITY) as Int
                invalidate()
            }
            start()
        }
        secondPulseAnimator = ValueAnimator().apply {
            setValues(propertyRadius, propertyRotate)
            startDelay = NEXT_ANIMATION_DELAY
            duration = ANIMATION_DURATION
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { valueAnimator ->
                secondPulseSize = valueAnimator.getAnimatedValue(PROPERTY_SIZE) as Float
                secondPulsePaint.alpha = valueAnimator.getAnimatedValue(PROPERTY_OPACITY) as Int
                invalidate()
            }
            start()
        }
    }

    fun stop() {
        circlePaint.color = inactiveColor
        stopAnimators(listOf(firstPulseAnimator, secondPulseAnimator))
        invalidate()
    }

    private fun stopAnimators(animators: List<ValueAnimator?>) {
        animators.forEach {
            it?.run {
                end()
                removeAllUpdateListeners()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    @Suppress("MagicNumber")
    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        canvas.drawCircle(width / 2f, height / 2f, circleSize / 2f, circlePaint)
        canvas.drawCircle(width / 2f, height / 2f, height / 2f * firstPulseSize, firstPulsePaint)
        canvas.drawCircle(
            width / 2f,
            height / 2f,
            height / 2f * secondPulseSize,
            secondPulsePaint
        )
    }

    companion object {
        private const val STROKE_WIDTH = 2f
        private const val ANIMATION_DURATION = 3000L
        private const val NEXT_ANIMATION_DELAY = 1000L
        private const val MAX_ALPHA = 255
        private const val MIN_ALPHA = 0
        private const val MIN_SIZE = 0f
        private const val MAX_SIZE = 0.9f

        private const val PROPERTY_SIZE = "PROPERTY_SIZE"
        private const val PROPERTY_OPACITY = "PROPERTY_OPACITY"
    }
}
