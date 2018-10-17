package co.netguru.baby.monitor.client.common.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.support.annotation.AttrRes
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.internal.operators.single.SingleDefer
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Context.getDrawableCompat(@DrawableRes drawable: Int) =
        ContextCompat.getDrawable(this, drawable)

fun Context.getAttributeColor(@AttrRes attrColor: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrColor, typedValue, true)
    return typedValue.data
}

fun Context.getAttributeDrawable(@AttrRes attrDrawableRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrDrawableRes, typedValue, true)
    return typedValue.resourceId
}

fun Context.allPermissionsGranted(permissions: Array<String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }

    return true
}

fun Context.saveAssetToCache(name: String) = Single.just(File(cacheDir.toString() + name))
        .map { file ->
            assets.open(name).use { inputStream ->
                val buffer = ByteArray(inputStream.available())
                inputStream.read(buffer)
                FileOutputStream(file).use {
                    it.write(buffer)
                }
                return@map file
            }
        }
