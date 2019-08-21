package co.netguru.baby.monitor.client.feature.babycrynotification

import android.support.v4.app.NotificationManagerCompat
import co.netguru.baby.monitor.client.common.NotificationHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class BabyMonitorMessagingService : FirebaseMessagingService() {

    private val notificationHandler by lazy { NotificationHandler(this) }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.i("Received a message: $message.")
        message.data?.let(::handleRemoteNotification)
    }

    private fun handleRemoteNotification(remoteNotification: Map<String, String>) {
        val title = remoteNotification[NOTIFICATION_TITLE_KEY].orEmpty()
        val body = remoteNotification[NOTIFICATION_CONTENT_KEY].orEmpty()

        NotificationHandler.createNotificationChannel(this)
        NotificationManagerCompat.from(this).notify(
            0,
            notificationHandler.createNotification(title = title, content = body)
        )
    }

    companion object {
        const val NOTIFICATION_TITLE_KEY = "title"
        const val NOTIFICATION_CONTENT_KEY = "text"
    }
}
