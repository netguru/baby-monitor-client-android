package co.netguru.baby.monitor.client.common.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.support.annotation.AttrRes
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.util.TypedValue

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
