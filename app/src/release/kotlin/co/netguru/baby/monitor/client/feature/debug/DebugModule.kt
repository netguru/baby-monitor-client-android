package co.netguru.baby.monitor.client.feature.debug

import io.reactivex.Observable
import javax.inject.Inject

class DebugModule @Inject constructor() {
    fun sendCryingProbabilityEvent(cryingProbability: Float) {}
    fun sendNotificationEvent(notificationInformation: String) {}
    fun debugStateObservable(): Observable<DebugState> = Observable.empty()
}
