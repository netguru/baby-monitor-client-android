package co.netguru.baby.monitor.client.feature.communication.webrtc.client

import android.content.Context
import co.netguru.baby.monitor.client.common.view.CustomSurfaceViewRenderer
import co.netguru.baby.monitor.client.feature.communication.webrtc.StreamState
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageParser
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

class RtcClientController @Inject constructor(
    private val messageParser: MessageParser
) {
    private var rtcClient: RtcClient? = null
    private val compositeDisposable = CompositeDisposable()

    fun startCall(
        context: Context,
        liveCameraRemoteRenderer: CustomSurfaceViewRenderer,
        serverUri: URI,
        client: RxWebSocketClient,
        streamStateListener: (streamState: StreamState) -> Unit
    ) {
        rtcClient = RtcClient(
            RtcClientMessageController(
                messageParser,
                serverUri,
                client
            ),
            streamStateListener,
            liveCameraRemoteRenderer
        ).apply {
            startCall(context)
                .subscribeOn(Schedulers.newThread())
                .subscribeBy(
                    onComplete = {
                        Timber.i("Call started")
                    },
                    onError = {
                        endCall()
                        Timber.e(it, "Error during startCall")
                    }
                ).addTo(compositeDisposable)
        }
    }

    fun pushToSpeak(isEnabled: Boolean) {
        rtcClient?.microphoneEnabled = isEnabled
    }

    fun endCall() {
        compositeDisposable.clear()
        rtcClient?.cleanup()
        rtcClient = null
    }
}
