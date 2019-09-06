package co.netguru.baby.monitor.client.application

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.babycrynotification.NotifyBabyCryingUseCase
import co.netguru.baby.monitor.client.feature.batterylevel.NotifyLowBatteryUseCase
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class DebugNotificationManager @Inject constructor(
    val notifyBabyCryingUseCase: NotifyBabyCryingUseCase,
    val notifyLowBatteryUseCase: NotifyLowBatteryUseCase
) {

    private val receiver = DebugNotificationReceiver()

    fun show(context: Context) {
        val channelId = "debug"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Debug Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        receiver.register(context)
        NotificationManagerCompat.from(context)
            .notify(-1, createDebugNotification(context = context, channelId = channelId))
    }

    private fun createDebugNotification(context: Context, channelId: String): Notification =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Debug Notification")
            .setOngoing(true)
            .addAction(
                0,
                "Cry",
                PendingIntent.getBroadcast(
                    context,
                    1,
                    receiver.cryingBabyIntent(),
                    0
                )
            )
            .addAction(
                0,
                "Low Battery",
                PendingIntent.getBroadcast(
                    context,
                    2,
                    receiver.lowBatteryIntent(),
                    0
                )
            )
            .build()

    private inner class DebugNotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ACTION_DEBUG_NOTIFICATION)
                throw RuntimeException("Unhandled action: {intent.action}.")

            when (intent.getSerializableExtra(KEY_DEBUG_NOTIFICATION_EXTRA) as DebugNotificationAction) {
                DebugNotificationAction.BABY_CRYING -> {
                    notifyBabyCryingUseCase.notifyBabyCrying()
                }
                DebugNotificationAction.LOW_BATTERY -> {
                    notifyLowBatteryUseCase.notifyLowBattery(
                        context.getString(R.string.notification_low_battery_title),
                        context.getString(R.string.notification_low_battery_text)
                    )
                        .subscribeOn(Schedulers.io())
                        .subscribeBy(
                            onComplete = {
                                Timber.d("Notified about low battery.")
                            },
                            onError = { error ->
                                Timber.i(error, "Couldn't notify about low battery.")
                            }
                        )
                }
            }
        }

        internal fun register(context: Context) {
            context.registerReceiver(this, IntentFilter(ACTION_DEBUG_NOTIFICATION))
        }

        private fun intent(action: DebugNotificationAction) =
            Intent(ACTION_DEBUG_NOTIFICATION).apply {
                putExtra(KEY_DEBUG_NOTIFICATION_EXTRA, action)
            }

        internal fun cryingBabyIntent() =
            intent(DebugNotificationAction.BABY_CRYING)

        internal fun lowBatteryIntent() =
            intent(DebugNotificationAction.LOW_BATTERY)
    }

    private enum class DebugNotificationAction {
        BABY_CRYING,
        LOW_BATTERY,
    }

    companion object {
        private const val ACTION_DEBUG_NOTIFICATION = "co.netguru.baby.DEBUG_NOTIFICATION"
        private const val KEY_DEBUG_NOTIFICATION_EXTRA = "co.netguru.baby.KEY_DEBUG_NOTIFICATION_EXTRA"
    }
}
