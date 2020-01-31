package co.netguru.baby.monitor.client.feature.babynotification

import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.Event
import co.netguru.baby.monitor.client.feature.analytics.EventType
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SnoozeNotificationUseCase @Inject constructor(
    private val dataRepository: DataRepository,
    private val analyticsManager: AnalyticsManager
) {

    fun snoozeNotifications(): Disposable {
        analyticsManager.logEvent(Event.Simple(EventType.NOTIFICATION_SNOOZE))
        return dataRepository
            .updateChildSnoozeTimestamp(System.currentTimeMillis() + SNOOZE_TIME)
            .subscribeOn(Schedulers.io())
            .subscribe { Timber.i("Notification snooze timestamp updated") }
    }

    companion object {
        private val SNOOZE_TIME = TimeUnit.MINUTES.toMillis(5)
        const val SNOOZE_DIALOG_TAG = "SNOOZE_DIALOG"
    }
}
