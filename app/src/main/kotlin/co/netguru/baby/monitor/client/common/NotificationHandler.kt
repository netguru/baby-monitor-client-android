package co.netguru.baby.monitor.client.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import co.netguru.baby.monitor.client.feature.babycrynotification.CryingActionIntentService
import co.netguru.baby.monitor.client.feature.server.ServerActivity
import org.jetbrains.anko.singleTop

class NotificationHandler(private val context: Context) {

    fun showForegroundNotification(service: Service) {
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            R.drawable.top_monitoring_icon
        } else {
            R.mipmap.ic_launcher
        }

        val notification =
            NotificationCompat.Builder(service, context.getString(R.string.notification_channel_id))
                .setOngoing(true)
                .setSmallIcon(drawableResId)
                .setContentTitle(service.getString(R.string.notification_foreground_content_title))
                .setContentText(service.getString(R.string.notification_foreground_content_text))
                .setContentIntent(
                    PendingIntent.getActivity(
                        service,
                        0,
                        Intent(service, ServerActivity::class.java).singleTop(),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
                .build()

        createNotificationChannel(service)

        service.startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    fun createNotification(title: String, content: String, iconResId: Int? = null): Notification {
        val resultIntent = Intent(context, ClientHomeActivity::class.java).singleTop()
        val resultPendingIntent =
            PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
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
            .addAction(createNotificationAction(CAMERA_REQUEST_CODE, SHOW_CAMERA_ACTION))
            .addAction(createNotificationAction(SNOOZE_REQUEST_CODE, SNOOZE_ACTION))
            .build()
    }

    private fun getActionPendingIntent(requestCode: Int, action: String): PendingIntent {
        return PendingIntent.getService(
            context,
            requestCode,
            Intent(context, CryingActionIntentService::class.java).apply {
                this.action = action
            },
            0
        )
    }

    private fun createNotificationAction(
        requestCode: Int,
        action: String
    ): NotificationCompat.Action {
        val title = when (action) {
            SHOW_CAMERA_ACTION -> context.resources.getString(R.string.notification_show_camera)
            SNOOZE_ACTION -> context.resources.getString(R.string.notification_5_minutes_snooze)
            else -> ""
        }
        return NotificationCompat.Action(
            0,
            title,
            getActionPendingIntent(requestCode, action)
        )
    }

    fun clearNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 1_001
        const val SNOOZE_ACTION = "SNOOZE_ACTION"
        const val SHOW_CAMERA_ACTION = "SHOW_CAMERA_ACTION"
        private const val SNOOZE_REQUEST_CODE = 456
        private const val CAMERA_REQUEST_CODE = 789

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
