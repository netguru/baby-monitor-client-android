package co.netguru.baby.monitor.client.feature.client.home.livecamera

import android.arch.lifecycle.ViewModel
import android.content.Context
import co.netguru.baby.monitor.client.common.view.CustomSurfaceViewRenderer
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.StreamState
import co.netguru.baby.monitor.client.feature.communication.webrtc.client.RtcClient
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ClientLiveCameraFragmentViewModel @Inject constructor() : ViewModel() {

    private var currentCall: RtcClient? = null
    val callInProgress = AtomicBoolean(false)
    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        endCall()
    }

    fun startCall(
        context: Context,
        liveCameraRemoteRenderer: CustomSurfaceViewRenderer,
        serverUri: URI,
        client: RxWebSocketClient,
        listener: (state: CallState) -> Unit,
        streamStateListener: (streamState: StreamState) -> Unit
    ) {
        callInProgress.set(true)
        currentCall = RtcClient(client = client, serverUri = serverUri).apply {

            startCall(context, listener, streamStateListener)
                .subscribeOn(Schedulers.newThread())
                .subscribeBy(
                    onComplete = {
                        Timber.i("Call started")
                    },
                    onError = {
                        cleanup()
                        callInProgress.set(false)
                        Timber.e(it, "Error during startCall")
                    }
                ).addTo(compositeDisposable)
            remoteView = liveCameraRemoteRenderer
        }
    }

    fun endCall() {
        compositeDisposable.clear()
        currentCall?.cleanup()
        currentCall = null
        callInProgress.set(false)
    }
}
