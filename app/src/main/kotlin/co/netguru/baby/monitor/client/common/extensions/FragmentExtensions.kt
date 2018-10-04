package co.netguru.baby.monitor.client.common.extensions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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

fun Fragment.startAppSettings() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).run {
        addCategory(Intent.CATEGORY_DEFAULT)
        data = Uri.parse("package:" + requireContext().packageName)
        startActivity(this)
    }
}
