package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.client.data.DataRepository
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SnoozeNotificationUseCase @Inject constructor(private val dataRepository: DataRepository) {

    fun snoozeNotifications(): Disposable {
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
