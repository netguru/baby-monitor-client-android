package co.netguru.baby.monitor.client.feature.batterylevel

import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import timber.log.Timber
import javax.inject.Inject

class NotifyLowBatteryUseCase @Inject constructor(
    private val notificationSender: FirebaseNotificationSender
) {
    fun notifyLowBattery(title: String, text: String) =
        notificationSender.broadcastNotificationToFcm(title = title, text = text).also {
            Timber.d("notifyLowBattery($title, $text)")
        }
}
