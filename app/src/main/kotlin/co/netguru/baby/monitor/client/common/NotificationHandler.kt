package co.netguru.baby.monitor.client.common

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.data.communication.firebase.FirebasePushMessage
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import co.netguru.baby.monitor.client.feature.server.ServerActivity
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.singleTop

class NotificationHandler(private val context: Context) {

    fun showForegroundNotification(service: Service) {
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            R.drawable.top_monitoring_icon else R.mipmap.ic_launcher

        val notification = NotificationCompat.Builder(service, context.getString(R.string.notification_channel_id))
            .setOngoing(true)
            .setSmallIcon(drawableResId)
            .setContentTitle(service.getString(R.string.notification_foreground_content_title))
            .setContentText(service.getString(R.string.notification_foreground_content_text))
            .setContentIntent(PendingIntent.getActivity(service, 0, Intent(service, ServerActivity::class.java).singleTop(), PendingIntent.FLAG_UPDATE_CURRENT))
            .build()

        createNotificationChannel(service)

        service.startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    fun showBabyIsCryingNotification() {
        createNotificationChannel(context)

        val notification = createNotification(
            context.getString(R.string.notification_baby_is_crying_title),
            context.getString(R.string.notification_baby_is_crying_content)
        )

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICAITON_ID, notification)
        }
    }

    fun createNotification(title: String, content: String, iconResId: Int? = null): Notification {
        val resultIntent = Intent(context, ClientHomeActivity::class.java).singleTop()
        val resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationBuilder =
            NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))

        iconResId?.let { icon ->
            notificationBuilder.setSmallIcon(icon)
        }

        return notificationBuilder
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .build()
    }

    fun sendFirabaseBabyIsCryingNotification(to: String) {
        val message = FirebasePushMessage(to, context.getString(R.string.notification_baby_is_crying_title),
                context.getString(R.string.notification_baby_is_crying_content))
        FirebaseDatabase.getInstance().getReference()
                .child(FIREBASE_NOTIFICATIONS_DB_PATH)
                .push()
                .setValue(message)
    }

    fun clearNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 1_001
        const val NOTIFICAITON_ID = 1
        const val FIREBASE_NOTIFICATIONS_DB_PATH = "notifications"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.notification_channel_name)
                val descriptionText = context.getString(R.string.notification_channel_description)
                val importance = NotificationManager.IMPORTANCE_HIGH

                val channel = NotificationChannel(
                        context.getString(R.string.notification_channel_id),
                        name,
                        importance
                ).apply {
                    description = descriptionText
                }

                val notificationManager: NotificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
