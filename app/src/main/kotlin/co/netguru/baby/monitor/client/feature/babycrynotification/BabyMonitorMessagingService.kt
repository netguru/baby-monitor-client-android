package co.netguru.baby.monitor.client.feature.babycrynotification

import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.NotificationHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class BabyMonitorMessagingService : FirebaseMessagingService() {

    private val notificationHandler by lazy { NotificationHandler(this) }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.i("Received a message: $message.")
        message.notification?.let(::handleRemoteNotification)
    }

    private fun handleRemoteNotification(remoteNotification: RemoteMessage.Notification) {
        val title = remoteNotification.title.orEmpty()
        val body = remoteNotification.body.orEmpty()
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            R.drawable.white_logo else R.mipmap.ic_launcher
        NotificationHandler.createNotificationChannel(this)

        NotificationManagerCompat.from(this).notify(
            0,
            notificationHandler.createNotification(title = title, content = body, iconResId = drawableResId)
        )
    }
}
