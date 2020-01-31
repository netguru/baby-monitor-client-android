package co.netguru.baby.monitor.client.feature.babynotification

import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Reusable
class NotifyBabyEventUseCase(
    private val notificationSender: FirebaseNotificationSender,
    private val notificationTexts: Map<String, String>
) {
    private val babyCryingEvents: PublishSubject<BabyEvent> = PublishSubject.create()

    private fun fetchClientsAndPostNotification(babyEvent: BabyEvent): Completable {
        val (notificationTitle, notificationType) = when (babyEvent) {
            BabyEvent.BabyCrying -> notificationTexts[CRY_TITLE_KEY] to NotificationType.CRY_NOTIFICATION
            BabyEvent.NoiseDetected -> notificationTexts[NOISE_TITLE_KEY] to NotificationType.NOISE_NOTIFICATION
        }
        return notificationSender.broadcastNotificationToFcm(
            notificationTitle ?: error("Notification title missing"),
            notificationTexts[NOTIFICATION_TEXT_KEY] ?: error("Notification text missing"),
            notificationType
        )
    }

    init {
        babyCryingEvents
            .throttleFirst(CRYING_EVENTS_THROTTLING_TIME, TimeUnit.MINUTES)
            .flatMapCompletable {
                fetchClientsAndPostNotification(it)
                    .subscribeOn(Schedulers.io())
            }
            .doOnError { Timber.w(it) }
            .retry()
            .subscribe()
    }

    fun notifyBabyCrying() =
        babyCryingEvents.onNext(BabyEvent.BabyCrying)

    fun notifyNoiseDetected() =
        babyCryingEvents.onNext(BabyEvent.NoiseDetected)

    private sealed class BabyEvent {
        object BabyCrying : BabyEvent()
        object NoiseDetected : BabyEvent()
    }

    companion object {
        private const val CRYING_EVENTS_THROTTLING_TIME = 3L
        const val CRY_TITLE_KEY = "cry_title"
        const val NOISE_TITLE_KEY = "noise_title"
        const val NOTIFICATION_TEXT_KEY = "notification_text"
    }
}
