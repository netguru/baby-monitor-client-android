package co.netguru.baby.monitor.client.feature.communication.webrtc.observers

import co.netguru.baby.monitor.client.feature.communication.webrtc.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import timber.log.Timber

class ConnectionObserver : DefaultObserver() {

    private val streamSubject = PublishSubject.create<StreamState>()
    val streamObservable: Observable<StreamState> = streamSubject.flatMap {
        Observable.just(it)
    }

    fun onAcceptOffer() {
        streamSubject.onNext(ConnectionState(RtcConnectionState.ConnectionOffer))
    }

    fun onSetDescriptionError() {
        streamSubject.onNext(ConnectionState(RtcConnectionState.Error))
    }

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState?) {
        streamSubject.onNext(GatheringState(iceGatheringState))
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        streamSubject.onNext(OnIceCandidatesChange(OnIceCandidateAdded(iceCandidate)))
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<out IceCandidate>?) {
        streamSubject.onNext(OnIceCandidatesChange(OnIceCandidatesRemoved(iceCandidates)))
    }

    override fun onAddStream(mediaStream: MediaStream) {
        streamSubject.onNext(OnAddStream(mediaStream))
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
        Timber.i("onIceConnectionChange ${iceConnectionState?.name}")

        val rtcConnectionState = when (iceConnectionState) {
            PeerConnection.IceConnectionState.CHECKING -> RtcConnectionState.Checking
            PeerConnection.IceConnectionState.COMPLETED -> RtcConnectionState.Completed
            PeerConnection.IceConnectionState.CONNECTED -> RtcConnectionState.Connected
            PeerConnection.IceConnectionState.DISCONNECTED -> RtcConnectionState.Disconnected
            PeerConnection.IceConnectionState.FAILED -> RtcConnectionState.Error
            PeerConnection.IceConnectionState.CLOSED -> {
                streamSubject.onComplete()
                null
            }
            else -> null
        }
        rtcConnectionState?.run { streamSubject.onNext(ConnectionState(this)) }
    }
}
