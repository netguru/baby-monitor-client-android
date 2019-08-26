package co.netguru.baby.monitor.client.feature.communication.webrtc

import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultObserver
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.subjects.SingleSubject
import org.webrtc.*
import timber.log.Timber

private class ConnectionObserver(
    private val emitter: ObservableEmitter<StreamState>,
    private val mediaStreamHandler: ((MediaStream)) -> Unit,
    private val dataChannelHandler: ((DataChannel?) -> Unit)
) : DefaultObserver() {

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState?) {
        emitter.onNext(GatheringState(iceGatheringState))
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
        Timber.i("onIceConnectionChange ${iceConnectionState?.name}")
        emitter.onNext(ConnectionState(iceConnectionState))
    }

    override fun onAddStream(mediaStream: MediaStream) {
        mediaStream.let(mediaStreamHandler)
    }

    override fun onDataChannel(dataChannel: DataChannel?) {
        dataChannelHandler(dataChannel)
    }

}

fun PeerConnectionFactory.createPeerConnection(
    mediaConstraints: MediaConstraints,
    mediaStreamHandler: (MediaStream) -> Unit,
    dataChannelHandler: (DataChannel?) -> Unit
): Pair<SingleSubject<PeerConnection>, Observable<StreamState>> {

    val peerConnection: SingleSubject<PeerConnection> = SingleSubject.create()
    val streamStateObservable: Observable<StreamState> = Observable.create {
        peerConnection.onSuccess(
            createPeerConnection(
                emptyList(),
                mediaConstraints,
                ConnectionObserver(it, mediaStreamHandler, dataChannelHandler)
            )
        )


    }

    return peerConnection to streamStateObservable

}
