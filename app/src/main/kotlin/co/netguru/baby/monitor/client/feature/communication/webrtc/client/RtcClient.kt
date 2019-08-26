package co.netguru.baby.monitor.client.feature.communication.webrtc.client

import android.content.Context
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.data.communication.websocket.ConnectionStatus
import co.netguru.baby.monitor.client.feature.communication.webrtc.ConnectionState
import co.netguru.baby.monitor.client.feature.communication.webrtc.GatheringState
import co.netguru.baby.monitor.client.feature.communication.webrtc.StreamState
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcCall
import co.netguru.baby.monitor.client.feature.communication.webrtc.createPeerConnection
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultSdpObserver
import co.netguru.baby.monitor.client.feature.communication.websocket.CustomWebSocketClient
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.json.JSONObject
import org.webrtc.DataChannel
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import timber.log.Timber
import java.nio.charset.Charset

class RtcClient(
    client: CustomWebSocketClient,
    var enableVoice: Boolean = false
) : RtcCall() {

    init {
        commSocket = client
    }

    internal fun startCall(
        context: Context,
        callStateListener: (state: CallState) -> Unit,
        streamStateListener: (streamState: StreamState) -> Unit
    ) = Completable.fromAction {
        initRtc(context)
        this.callStateListener = callStateListener
        this.streamStateListener = streamStateListener
        Timber.i("starting call to ${(commSocket as CustomWebSocketClient?)?.address}")
        createConnection()
    }

    private fun createConnection() {
        val conState = factory?.createPeerConnection(
            constraints,
            this::handleMediaStream,
            this::handleDataChannel
        )
        conState?.first?.subscribe { peerConnection ->
            handleCreatedConnection(peerConnection)
        }

        conState?.second
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { streamState ->
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

    override fun createStream(): MediaStream? {
        upStream = factory?.createLocalMediaStream(LOCAL_MEDIA_STREAM_LABEL)
        audioSource = factory?.createAudioSource(MediaConstraints())
        audio = factory?.createAudioTrack(AUDIO_TRACK_ID, audioSource)
        upStream?.addTrack(audio)
        videoTrack = createVideoTrack()
        upStream?.addTrack(videoTrack)
        capturer?.startCapture(VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_FPS)
        audio?.setEnabled(enableVoice)
        return upStream
    }

    private fun onIceGatheringComplete() {
        with((commSocket as CustomWebSocketClient)) {
            if (connectionStatus == ConnectionStatus.CONNECTED) {
                sendOffer(this)
            }

            addMessageListener { client, message ->
                val jsonObject = JSONObject(message)
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

    private fun sendOffer(client: CustomWebSocketClient) {
        val jsonObject = JSONObject().apply {
            put(
                    P2P_OFFER,
                    JSONObject().apply {
                        put("sdp", connection?.localDescription?.description)
                        put("type", connection?.localDescription?.type?.canonicalForm())
                    }
            )
        }
        client.send(jsonObject.toString().toByteArray(Charset.defaultCharset()))
        Timber.i("offer send: $jsonObject")
    }
}
