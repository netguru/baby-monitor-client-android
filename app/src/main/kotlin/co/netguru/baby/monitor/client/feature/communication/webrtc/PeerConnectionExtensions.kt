package co.netguru.baby.monitor.client.feature.communication.webrtc

import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Single
import io.reactivex.SingleEmitter
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

private class SetSdpObserver(private val emitter: CompletableEmitter) : SdpObserver {
    override fun onSetSuccess() {
        emitter.onComplete()
    }

    override fun onSetFailure(error: String?) {
        emitter.onError(Throwable(error))
    }

    override fun onCreateSuccess(sessionDescription: SessionDescription) =
        throw RuntimeException()

    override fun onCreateFailure(error: String?) =
        throw RuntimeException()
}

fun PeerConnection.setRemoteDescription(sessionDescription: SessionDescription) = Completable.create { emitter ->
    setRemoteDescription(SetSdpObserver(emitter), sessionDescription)
}

fun PeerConnection.setLocalDescription(sessionDescription: SessionDescription) = Completable.create { emitter ->
    setLocalDescription(SetSdpObserver(emitter), sessionDescription)
}

private class CreateSdpObserver(private val emitter: SingleEmitter<SessionDescription>) : SdpObserver {
    override fun onCreateSuccess(sessionDescription: SessionDescription) {
        emitter.onSuccess(sessionDescription)
    }

    override fun onCreateFailure(error: String?) {
        emitter.onError(Throwable(error))
    }

    override fun onSetFailure(error: String?) =
        throw RuntimeException()

    override fun onSetSuccess() =
        throw RuntimeException()
}

fun PeerConnection.createAnswer(): Single<SessionDescription> = Single.create { emitter ->
    createAnswer(CreateSdpObserver(emitter), MediaConstraints())
}

fun PeerConnection.createOffer(): Single<SessionDescription> = Single.create { emitter ->
    createOffer(CreateSdpObserver(emitter), MediaConstraints())
}
