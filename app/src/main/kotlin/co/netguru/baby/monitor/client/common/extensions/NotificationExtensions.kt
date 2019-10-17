package co.netguru.baby.monitor.client.common.extensions

import android.support.v4.app.NotificationCompat

fun NotificationCompat.Builder.addActions(actions: List<NotificationCompat.Action>?): NotificationCompat.Builder {
    actions?.forEach {
        addAction(it)
    }
    return this
}
