package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import dagger.Reusable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class NotifyBabyCryingUseCase @Inject constructor(
    private val notificationSender: FirebaseNotificationSender,
    private val title: String,
    private val text: String
) {
    private val babyCryingEvents: PublishSubject<BabyCrying> = PublishSubject.create()

    private fun fetchClientsAndPostNotification() =
        notificationSender.broadcastNotificationToFcm(
            title,
            text,
            NotificationType.CRY_NOTIFICATION
        )

    init {
        babyCryingEvents
            .throttleFirst(CRYING_EVENTS_THROTTLING_TIME, TimeUnit.MINUTES)
            .flatMapCompletable {
                fetchClientsAndPostNotification()
            }
            .doOnError { Timber.w(it) }
            .retry()
            .subscribe()
    }

    fun notifyBabyCrying() =
        babyCryingEvents.onNext(BabyCrying)

    private object BabyCrying

    companion object {
        private const val CRYING_EVENTS_THROTTLING_TIME = 3L
    }
}
