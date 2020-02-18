package co.netguru.baby.monitor.client.feature.batterylevel

import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationData
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import timber.log.Timber
import javax.inject.Inject

class NotifyLowBatteryUseCase @Inject constructor(
    private val notificationSender: FirebaseNotificationSender
) {
    fun notifyLowBattery(title: String, text: String) =
        notificationSender.broadcastNotificationToFcm(
            notificationData = NotificationData(
                title,
                text,
                NotificationType.LOW_BATTERY_NOTIFICATION
            )
        ).also {
            Timber.d("notifyLowBattery($title, $text)")
        }
}
