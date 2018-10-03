package co.netguru.baby.monitor.client.common.extensions

import android.view.View

fun View.setVisible(boolean: Boolean) {
    this.visibility = if (boolean) {
        View.VISIBLE
    } else {
        View.GONE
    }
}
