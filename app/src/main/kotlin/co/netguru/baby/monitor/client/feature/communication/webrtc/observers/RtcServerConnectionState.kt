package co.netguru.baby.monitor.client.feature.communication.webrtc.observers

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.webrtc.PeerConnection

class RtcServerConnectionObserver : DefaultObserver() {
    private val publishSubject = PublishSubject.create<RtcServerConnectionState>()
    val rtcConnectionObservable: Observable<RtcServerConnectionState> = publishSubject

    fun onAcceptOffer() {
        publishSubject.onNext(RtcServerConnectionState.ConnectionOffer)
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
        super.onIceConnectionChange(iceConnectionState)
        when (iceConnectionState) {
            PeerConnection.IceConnectionState.CONNECTED -> publishSubject.onNext(
                RtcServerConnectionState.Connected
            )
            PeerConnection.IceConnectionState.DISCONNECTED -> publishSubject.onNext(
                RtcServerConnectionState.Disconnected
            )
            PeerConnection.IceConnectionState.CLOSED -> publishSubject.onComplete()
            else -> Unit
        }
    }
}

sealed class RtcServerConnectionState {
    object ConnectionOffer : RtcServerConnectionState()
    object Connected : RtcServerConnectionState()
    object Disconnected : RtcServerConnectionState()
    object Error : RtcServerConnectionState()
}
