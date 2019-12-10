package co.netguru.baby.monitor.client.feature.debug

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.babycrynotification.NotifyBabyCryingUseCase
import co.netguru.baby.monitor.client.feature.batterylevel.NotifyLowBatteryUseCase
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugNotificationManager @Inject constructor(
    val notifyBabyCryingUseCase: NotifyBabyCryingUseCase,
    val notifyLowBatteryUseCase: NotifyLowBatteryUseCase
) {

    private val receiver = DebugNotificationReceiver()

    fun show(service: Service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(
            service
        )
        receiver.register(service)
        service.startForeground(DEBUG_NOTIFICATION_ID, createDebugNotification(service))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(service: Service) {
        val channel = NotificationChannel(
            DEBUG_CHANNEL_ID,
            "Debug Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager =
            service.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun clear(context: Context) {
        NotificationManagerCompat.from(context).cancel(DEBUG_NOTIFICATION_ID)
        context.unregisterReceiver(receiver)
    }

    private fun createDebugNotification(service: Service): Notification =
        NotificationCompat.Builder(service, DEBUG_CHANNEL_ID)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Debug notification ")
            .addAction(
                NO_ICON,
                "Cry",
                PendingIntent.getBroadcast(
                    service,
                    CRY_ACTION_REQUEST_CODE,
                    receiver.cryingBabyIntent(),
                    NO_FLAGS
                )
            )
            .addAction(
                NO_ICON,
                "Low Battery",
                PendingIntent.getBroadcast(
                    service,
                    LOW_BATTERY_ACTION_REQUEST_CODE,
                    receiver.lowBatteryIntent(),
                    NO_FLAGS
                )
            )
            .build()

    private inner class DebugNotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            require(intent.action == ACTION_DEBUG_NOTIFICATION) { "Unhandled action: {intent.action}." }

            when (intent.getSerializableExtra(KEY_DEBUG_NOTIFICATION_EXTRA) as DebugNotificationAction) {
                DebugNotificationAction.BABY_CRYING -> notifyBabyCryingUseCase.notifyBabyCrying()

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
        private const val KEY_DEBUG_NOTIFICATION_EXTRA =
            "co.netguru.baby.KEY_DEBUG_NOTIFICATION_EXTRA"
        private const val DEBUG_NOTIFICATION_ID = 987
        private const val NO_ICON = 0
        private const val NO_FLAGS = 0
        private const val CRY_ACTION_REQUEST_CODE = 1
        private const val LOW_BATTERY_ACTION_REQUEST_CODE = 2
        private const val DEBUG_CHANNEL_ID = "debug"
    }
}
