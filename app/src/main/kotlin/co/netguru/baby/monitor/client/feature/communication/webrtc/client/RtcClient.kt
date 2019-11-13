package co.netguru.baby.monitor.client.feature.communication.webrtc.client

import android.content.Context
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.ConnectionState
import co.netguru.baby.monitor.client.feature.communication.webrtc.GatheringState
import co.netguru.baby.monitor.client.feature.communication.webrtc.StreamState
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcCall
import co.netguru.baby.monitor.client.feature.communication.webrtc.createPeerConnection
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultSdpObserver
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import org.webrtc.*
import timber.log.Timber
import java.net.URI

class RtcClient(
    client: RxWebSocketClient,
    private val serverUri: URI
) : RtcCall(client) {

    internal fun startCall(
        context: Context,
        callStateListener: (state: CallState) -> Unit,
        streamStateListener: (streamState: StreamState) -> Unit
    ) = Completable.fromAction {
        initRtc(context)
        this.callStateListener = callStateListener
        this.streamStateListener = streamStateListener
        createConnection(factory ?: return@fromAction)
    }

    private fun createConnection(factory: PeerConnectionFactory) {
        val (peerConnection, stateStream) = factory.createPeerConnection(
            this::handleMediaStream,
            this::handleDataChannel
        )
        peerConnection
            .subscribe(::handleCreatedConnection)
            .addTo(compositeDisposable)

        stateStream
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { streamState ->
                when (streamState) {
                    is ConnectionState -> Unit
                    is GatheringState -> {
                        if (streamState.gatheringState == PeerConnection.IceGatheringState.COMPLETE) {
                            onIceGatheringComplete()
                        }
                    }
                }
                reportStreamStateChange(streamState)
            }
            .addTo(compositeDisposable)
    }

    private fun handleCreatedConnection(peerConnection: PeerConnection) {
        connection = peerConnection
        Timber.i("PeerConnection created")
        dataChannel = connection?.createDataChannel("data", DataChannel.Init())
        dataChannel?.registerObserver(dataChannelObserver)
        connection?.createOffer(
            DefaultSdpObserver(
                onCreateSuccess = { sessionDescription ->
                    connection?.setLocalDescription(
                        DefaultSdpObserver(),
                        sessionDescription
                    )
                },
                onCreateFailure = {
                    Timber.d("sdb observer create failure")
                },
                onSetFailure = {
                    Timber.d("sdb set failure: $it")
                }
            ),
            constraints
        )
    }

    private fun onIceGatheringComplete() {
        if (client.isOpen()) sendOffer(client)
        client.events(serverUri = serverUri)
            .subscribeOn(Schedulers.io())
            .subscribe { event ->
                Timber.d("WS Event: $event.")
                when (event) {
                    is RxWebSocketClient.Event.Open -> {
                        Timber.i("socket open -> send offer")
                        sendOffer(client)
                    }
                    is RxWebSocketClient.Event.Message -> {
                        val jsonObject = JSONObject(event.message)
                        if (jsonObject.has(P2P_ANSWER)) {
                            if (state == CallState.CONNECTED) {
                                connection?.close()
                            }
                            reportStateChange(CallState.CONNECTED)
                            handleAnswer(jsonObject.getJSONObject(P2P_ANSWER).getString("sdp"))
                        }
                    }
                }
            }
            .addTo(compositeDisposable)
    }

    private fun sendOffer(client: RxWebSocketClient) {
        val jsonObject = JSONObject().apply {
            put(
                    P2P_OFFER,
                    JSONObject().apply {
                        put("sdp", connection?.localDescription?.description)
                        put("type", connection?.localDescription?.type?.canonicalForm())
                    }
            )
        }
        client.send(jsonObject.toString()).blockingAwait()
        Timber.i("offer send: $jsonObject")
    }
}
