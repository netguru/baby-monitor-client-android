package co.netguru.baby.monitor.client.common.extensions

import android.graphics.*
import android.support.design.widget.Snackbar
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import co.netguru.baby.monitor.client.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget

val BITMAP_AUTO_SIZE = -1f
internal val BORDER_TO_WIDTH_RATIO = 2f / 95f
internal val ICON_TO_WIDTH_RATIO = 27f / 96f

fun View.setVisible(boolean: Boolean) {
    this.visibility = if (boolean) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun View.showSnackbar(text: String, duration: Int) {
    Snackbar.make(this, text, duration).show()
}

fun EditText.afterTextChanged(afterChange: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterChange(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    })
}

val EditText.trimmedText: String
    get() = this.text.toString().trim()

fun Bitmap.addBorderAndCover(borderColor: Int, borderSize: Float, coverColor: Int, vectorDrawable: VectorDrawableCompat?): Bitmap {
    val smallerEdge = Math.min(height, width)
    var borderSizeFinal = borderSize
    if (borderSizeFinal < 0f) {
        borderSizeFinal = BORDER_TO_WIDTH_RATIO * smallerEdge;
    }
    val borderOffset = (borderSizeFinal * 2).toInt()
    val radius = smallerEdge / 2.toFloat()
    val output = Bitmap.createBitmap(width + borderOffset, height + borderOffset, Bitmap.Config.ARGB_8888)
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
    val rectf = RectF(borderSizeFinal / 2, borderSizeFinal / 2,
            width + borderSizeFinal * 1.5f, height + borderSizeFinal * 1.5f)
    canvas.drawArc(rectf, 0f, 180f, true, paint)
    if (vectorDrawable == null) {
        return output
    }
    vectorDrawable ?: return output
    val vectorWidth = ICON_TO_WIDTH_RATIO * (width + borderSizeFinal * 2)
    val vectorHeight = vectorDrawable.intrinsicHeight.toFloat() / vectorDrawable.intrinsicWidth.toFloat() * vectorWidth

    vectorDrawable.setBounds(0, 0, Math.round(vectorWidth), Math.round(vectorHeight))
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
                            resource?.addBorderAndCover(ContextCompat.getColor(context, R.color.accent), borderSize,
                                    ContextCompat.getColor(context, colorRes),
                                    VectorDrawableCompat.create(context.getResources(), R.drawable.ic_select_photo_camera, null))
                    )
                    circularBitmapDrawable.isCircular = true
                    setImageDrawable(circularBitmapDrawable)
                }
            })
}
