package co.netguru.baby.monitor.client.feature.communication.webrtc

import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultObserver
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.webrtc.*
import timber.log.Timber

private class ConnectionObserver(
    private val emitter: ObservableEmitter<StreamState>,
    private val mediaStreamHandler: ((MediaStream)) -> Unit,
    private val dataChannelHandler: ((DataChannel?) -> Unit)
) : DefaultObserver() {

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState?) {
        if (iceGatheringState == PeerConnection.IceGatheringState.COMPLETE) {
            emitter.onNext(StreamState.GATHERING_COMPLETE)
        }
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
        Timber.i("onIceConnectionChange ${iceConnectionState?.name}")
        if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
            emitter.onNext(StreamState.CONNECTED)
        }
    }

    override fun onAddStream(mediaStream: MediaStream) {
        mediaStream.let(mediaStreamHandler)
    }

    override fun onDataChannel(dataChannel: DataChannel?) {
        dataChannelHandler(dataChannel)
    }

}

enum class StreamState {
    GATHERING_COMPLETE, CONNECTED
}

fun PeerConnectionFactory.createPeerConnection(
    mediaConstraints: MediaConstraints,
    mediaStreamHandler: (MediaStream) -> Unit,
    dataChannelHandler: (DataChannel?) -> Unit
): Pair<Subject<PeerConnection>, Observable<StreamState>> {

    val peerConnection: Subject<PeerConnection> = PublishSubject.create()
    val streamStateObservable: Observable<StreamState> = Observable.create {
        peerConnection.onNext(
            createPeerConnection(
                emptyList(),
                mediaConstraints,
                ConnectionObserver(it, mediaStreamHandler, dataChannelHandler)
            )
        )


    }

    return peerConnection to streamStateObservable

}
