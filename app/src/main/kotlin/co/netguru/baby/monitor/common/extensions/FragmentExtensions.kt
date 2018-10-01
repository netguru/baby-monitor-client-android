package co.netguru.baby.monitor.common.extensions

import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment

fun Fragment.showSnackbarMessage(
    @StringRes resId: Int, action: (Snackbar.() -> Unit)? = null
): Snackbar? {
    return view?.run {
        Snackbar.make(this, resId, Snackbar.LENGTH_LONG).apply {
            action?.invoke(this)
            show()
        }
    }
}
