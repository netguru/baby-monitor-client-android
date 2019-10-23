package co.netguru.baby.monitor.client.common.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.View
import android.widget.ImageView
import co.netguru.baby.monitor.client.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import kotlin.math.roundToInt

const val BITMAP_AUTO_SIZE = -1f
internal const val BORDER_TO_WIDTH_RATIO = 2f / 95f
internal const val ICON_TO_WIDTH_RATIO = 27f / 96f

fun View.setVisible(boolean: Boolean) {
    this.visibility = if (boolean) {
        View.VISIBLE
    } else {
        View.GONE
    }
}
@Suppress("MagicNumber")
fun Bitmap.addBorderAndCover(
    borderColor: Int,
    borderSize: Float,
    coverColor: Int,
    vectorDrawable: VectorDrawableCompat?
): Bitmap {
    val smallerEdge = Math.min(height, width)
    var borderSizeFinal = borderSize
    if (borderSizeFinal < 0f) {
        borderSizeFinal = BORDER_TO_WIDTH_RATIO * smallerEdge
    }
    val borderOffset = (borderSizeFinal * 2).toInt()
    val radius = smallerEdge / 2.toFloat()
    val output =
        Bitmap.createBitmap(width + borderOffset, height + borderOffset, Bitmap.Config.ARGB_8888)
    val paint = Paint()
    val borderX = width / 2 + borderSizeFinal
    val borderY = height / 2 + borderSizeFinal
    val canvas = Canvas(output)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    if (borderSizeFinal > 0) {
        paint.style = Paint.Style.FILL
        canvas.drawCircle(borderX, borderY, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, borderSizeFinal, borderSizeFinal, paint)
        paint.xfermode = null
        paint.style = Paint.Style.STROKE
        paint.color = borderColor
        paint.strokeWidth = borderSizeFinal
        canvas.drawCircle(borderX, borderY, radius, paint)
    }
    paint.xfermode = null
    paint.style = Paint.Style.FILL
    paint.color = coverColor
    val rectf = RectF(
        borderSizeFinal / 2, borderSizeFinal / 2,
        width + borderSizeFinal * 1.5f, height + borderSizeFinal * 1.5f
    )
    canvas.drawArc(rectf, 0f, 180f, true, paint)
    vectorDrawable ?: return output
    val vectorWidth = ICON_TO_WIDTH_RATIO * (width + borderSizeFinal * 2)
    val vectorHeight =
        vectorDrawable.intrinsicHeight.toFloat() / vectorDrawable.intrinsicWidth.toFloat() * vectorWidth

    vectorDrawable.setBounds(0, 0, vectorWidth.roundToInt(), vectorHeight.roundToInt())
    val translateX = rectf.centerX() - vectorWidth / 2
    val translateY = 1.5f * rectf.centerY() - vectorHeight / 2
    canvas.translate(translateX, translateY)
    vectorDrawable.draw(canvas)
    canvas.translate(-translateX, -translateY)
    return output
}

fun <T> ImageView.babyProfileImage(uri: T, borderSize: Float, colorRes: Int, vectorDrawable: Int) {
    Glide.with(context)
        .asBitmap()
        .load(uri)
        .apply(RequestOptions.circleCropTransform())
        .into(object : BitmapImageViewTarget(this) {
            override fun setResource(resource: Bitmap?) {
                val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(
                    context.resources,
                    resource?.addBorderAndCover(
                        ContextCompat.getColor(context, R.color.accent), borderSize,
                        ContextCompat.getColor(context, colorRes),
                        VectorDrawableCompat.create(
                            context.resources,
                            vectorDrawable,
                            null
                        )
                    )
                )
                circularBitmapDrawable.isCircular = true
                setImageDrawable(circularBitmapDrawable)
            }
        })
}
