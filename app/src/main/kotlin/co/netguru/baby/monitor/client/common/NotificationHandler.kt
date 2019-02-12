package co.netguru.baby.monitor.client.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.data.communication.firebase.FirebasePushMessage
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.singleTop

class NotificationHandler(private val context: Context) {

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

    private fun createNotification(title: String, content: String): Notification {
        val resultIntent = Intent(context, ClientHomeActivity::class.java).singleTop()
        val resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            R.drawable.logo else R.mipmap.ic_launcher

        return NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
                .setSmallIcon(drawableResId)
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

    companion object {
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
