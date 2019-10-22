package co.netguru.baby.monitor.client.common.extensions

import androidx.core.app.NotificationCompat

fun NotificationCompat.Builder.addActions(actions: List<NotificationCompat.Action>?): NotificationCompat.Builder {
    actions?.forEach {
        addAction(it)
    }
    return this
}
