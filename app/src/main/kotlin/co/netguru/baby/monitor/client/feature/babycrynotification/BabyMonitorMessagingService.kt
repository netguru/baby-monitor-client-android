package co.netguru.baby.monitor.client.feature.babycrynotification

import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class BabyMonitorMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        Timber.i("Received a message: $message.")
        Toast.makeText(
            this,
            "Title: ${message.notification?.title}, body: ${message.notification?.body}.",
            Toast.LENGTH_LONG
        ).show()
    }
}
