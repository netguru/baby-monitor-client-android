package co.netguru.baby.monitor.client.feature.babynotification

import co.netguru.baby.monitor.client.feature.feedback.SavedRecordingDetails
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationData
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class NotifyBabyEventUseCase(
    private val notificationSender: FirebaseNotificationSender,
    private val notificationTexts: Map<String, String>
) {
    private val babyEvents: PublishSubject<BabyEvent> = PublishSubject.create()

    private fun fetchClientsAndPostNotification(babyEvent: BabyEvent): Completable {
        val notificationTitle = when (babyEvent) {
            BabyEvent.BabyCrying, is BabyEvent.BabyCryingFeedback
            -> notificationTexts[CRY_TITLE_KEY]
            BabyEvent.NoiseDetected, is BabyEvent.NoiseDetectedFeedback
            -> notificationTexts[NOISE_TITLE_KEY]
        }
        val notificationType = when (babyEvent) {
            BabyEvent.BabyCrying -> NotificationType.CRY_NOTIFICATION
            BabyEvent.NoiseDetected -> NotificationType.NOISE_NOTIFICATION
            is BabyEvent.BabyCryingFeedback -> NotificationType.CRY_NOTIFICATION_WITH_FEEDBACK_REQUEST
            is BabyEvent.NoiseDetectedFeedback -> NotificationType.NOISE_NOTIFICATION_WITH_FEEDBACK_REQUEST
        }
        val feedbackRecordingFile = when (babyEvent) {
            is BabyEvent.BabyCryingFeedback -> babyEvent.recordingName
            is BabyEvent.NoiseDetectedFeedback -> babyEvent.recordingName
            else -> ""
        }
        return notificationSender.broadcastNotificationToFcm(
            NotificationData(
                notificationTitle ?: error("Notification title missing"),
                notificationTexts[NOTIFICATION_TEXT_KEY] ?: error("Notification text missing"),
                notificationType,
                feedbackRecordingFile
            )
        )
    }

    fun babyEvents(): Completable {
        return babyEvents
            .throttleFirst(BABY_EVENTS_THROTTLING_TIME, TimeUnit.SECONDS)
            .flatMapCompletable {
                fetchClientsAndPostNotification(it)
                    .subscribeOn(Schedulers.io())
            }
            .doOnError { Timber.w(it) }
            .retry()
    }

    fun notifyBabyCrying(savedRecordingDetails: SavedRecordingDetails? = null) {
        val babyEvent = savedRecordingDetails?.let {
            if (it.shouldAskForFeedback) {
                BabyEvent.BabyCryingFeedback(it.fileName)
            } else {
                null
            }
        } ?: BabyEvent.BabyCrying
        babyEvents.onNext(babyEvent)
    }

    fun notifyNoiseDetected(savedRecordingDetails: SavedRecordingDetails? = null) {
        val noiseEvent = savedRecordingDetails?.let {
            if (it.shouldAskForFeedback) {
                BabyEvent.NoiseDetectedFeedback(it.fileName)
            } else {
                null
            }
        } ?: BabyEvent.NoiseDetected
        babyEvents.onNext(noiseEvent)
    }

    private sealed class BabyEvent {
        object BabyCrying : BabyEvent()
        object NoiseDetected : BabyEvent()
        data class BabyCryingFeedback(val recordingName: String) : BabyEvent()
        data class NoiseDetectedFeedback(val recordingName: String) : BabyEvent()
    }

    companion object {
        private const val BABY_EVENTS_THROTTLING_TIME = 15L
        const val CRY_TITLE_KEY = "cry_title"
        const val NOISE_TITLE_KEY = "noise_title"
        const val NOTIFICATION_TEXT_KEY = "notification_text"
    }
}
