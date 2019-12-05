package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import dagger.Reusable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class NotifyBabyCryingUseCase @Inject constructor(
    private val notificationSender: FirebaseNotificationSender
) {
    private val babyCryingEvents: PublishSubject<BabyCrying> = PublishSubject.create()
    private var babyCryingDisposable: Disposable? = null

    private fun fetchClientsAndPostNotification(
        title: String,
        text: String
    ) =
        notificationSender.broadcastNotificationToFcm(
            title,
            text,
            NotificationType.CRY_NOTIFICATION
        )

    fun subscribe(title: String, text: String) {
        babyCryingDisposable?.dispose()
        babyCryingDisposable = babyCryingEvents
            .throttleFirst(CRYING_EVENTS_THROTTLING_TIME, TimeUnit.MINUTES)
            .flatMapCompletable {
                fetchClientsAndPostNotification(
                    title = title,
                    text = text
                )
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
