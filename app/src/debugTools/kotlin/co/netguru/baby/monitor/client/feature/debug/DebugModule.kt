package co.netguru.baby.monitor.client.feature.debug

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugModule @Inject constructor() {
    private val cryingProbabilityEvents: PublishSubject<Float> = PublishSubject.create()
    private val notificationEvents: PublishSubject<String> = PublishSubject.create()

    fun sendCryingProbabilityEvent(cryingProbability: Float) {
        cryingProbabilityEvents.onNext(cryingProbability)
    }

    fun sendNotificationEvent(notificationInformation: String) {
        val currentDateTimeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
        notificationEvents.onNext("$notificationInformation at $currentDateTimeString")
    }

    fun debugStateObservable(): Observable<DebugState> {
        return Observable.combineLatest<String, Float, DebugState>(
            notificationEvents.startWith(NOTIFICATION_INFORMATION_INITIAL_STATE),
            cryingProbabilityEvents.startWith(CRYING_PROBABILITY_INITIAL_STATE),
            BiFunction<String, Float, DebugState> { notificationInformation: String, cryingProbability: Float ->
                DebugState(notificationInformation, cryingProbability)
            })
            .doOnError { Timber.w(it) }
            .retry()
    }

    companion object {
        private const val NOTIFICATION_INFORMATION_INITIAL_STATE = "No notification sent."
        private const val CRYING_PROBABILITY_INITIAL_STATE = 0f
        private const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
    }
}
