package co.netguru.baby.monitor.client.common.view

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class PulsatingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.parseColor(COLOR)
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.STROKE
    }
    private var animator: ValueAnimator? = null
    private var size = MIN_SIZE

    private val paint2 = Paint().apply {
        color = Color.parseColor(COLOR)
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.STROKE
    }
    private var animator2: ValueAnimator? = null
    private var size2 = MIN_SIZE

    fun start() {
        val propertyRadius = PropertyValuesHolder.ofFloat(PROPERTY_SIZE, MIN_SIZE, MAX_SIZE)
        val propertyRotate = PropertyValuesHolder.ofInt(PROPERTY_OPACITY, MAX_ALPHA, MIN_ALPHA)

        animator = ValueAnimator().apply {
            setValues(propertyRadius, propertyRotate)
            duration = ANIMATION_DURATION
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { valueAnimator ->
                size = valueAnimator.animatedValue as Float
                paint.alpha = valueAnimator.getAnimatedValue(PROPERTY_OPACITY) as Int
                invalidate()
            }
            start()
        }
        animator2 = ValueAnimator().apply {
            setValues(propertyRadius, propertyRotate)
            startDelay = NEXT_ANIMATION_DELAY
            duration = ANIMATION_DURATION
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { valueAnimator ->
                size2 = valueAnimator.getAnimatedValue(PROPERTY_SIZE) as Float
                paint2.alpha = valueAnimator.getAnimatedValue(PROPERTY_OPACITY) as Int
                invalidate()
            }
            start()
        }
    }

    fun stop() {
        animator?.end()
        animator2?.end()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        canvas.drawCircle(width / 2f, height / 2f, (height / 2f) * size, paint)
        canvas.drawCircle(width / 2f, height / 2f, (height / 2f) * size2, paint2)
    }

    companion object {
        private const val COLOR = "#33FF99"
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
