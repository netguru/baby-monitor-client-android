package co.netguru.baby.monitor.client.feature.communication.webrtc.client

import android.app.Service
import android.content.Context
import android.content.Intent
import co.netguru.baby.monitor.client.common.view.CustomSurfaceViewRenderer
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.WebRtcBinder
import co.netguru.baby.monitor.client.feature.communication.websocket.CustomWebSocketClient
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class WebRtcClientService : Service() {

    private lateinit var binder: WebRtcClientBinder
    private val compositeDisposable = CompositeDisposable()

    override fun onBind(intent: Intent?) = WebRtcClientBinder().also { binder = it }

    override fun onDestroy() {
        binder.cleanup()
        compositeDisposable.dispose()
        super.onDestroy()
    }

    inner class WebRtcClientBinder : WebRtcBinder() {

        var currentCall: RtcClient? = null

        override fun cleanup() {
            currentCall?.let(this::callCleanup)
        }

        fun createClient(client: CustomWebSocketClient) {
            currentCall = RtcClient(client)
        }

        fun setRemoteRenderer(liveCameraRemoteRenderer: CustomSurfaceViewRenderer) {
            currentCall?.remoteView = liveCameraRemoteRenderer
        }

        fun startCall(
                context: Context,
                listener: (state: CallState) -> Unit
        ) {
            currentCall?.let {
                it.startCall(context, listener).subscribeOn(Schedulers.newThread())
                        .subscribeBy(
                                onComplete = { Timber.i("completed") },
                                onError = Timber::e
                        ).addTo(compositeDisposable)
            }
        }

        private fun callCleanup(rtcClient: RtcClient) {
            rtcClient.cleanup()
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                            onComplete = {
                                Timber.i("call cleaned")
                            },
                            onError = Timber::e
                    ).addTo(compositeDisposable)
        }
    }
}
