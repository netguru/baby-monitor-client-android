package co.netguru.baby.monitor.client.feature.debug

import io.reactivex.Observable
import javax.inject.Inject

@Suppress("unused")
class DebugModule @Inject constructor() {
    fun sendCryingProbabilityEvent(cryingProbability: Float) = Unit
    fun sendNotificationEvent(notificationInformation: String) = Unit
    fun sendSoundEvent(decibels: Int) = Unit
    fun debugStateObservable(): Observable<DebugState> = Observable.empty()
}
