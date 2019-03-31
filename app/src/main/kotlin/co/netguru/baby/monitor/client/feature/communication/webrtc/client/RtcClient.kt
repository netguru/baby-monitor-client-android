package co.netguru.baby.monitor.client.feature.communication.webrtc.client

import android.content.Context
import co.netguru.baby.monitor.client.common.view.CustomSurfaceViewRenderer
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.data.communication.websocket.ConnectionStatus
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcCall
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultObserver
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultSdpObserver
import co.netguru.baby.monitor.client.feature.communication.websocket.CustomWebSocketClient
import io.reactivex.Completable
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
            listener: (state: CallState) -> Unit
    ) = Completable.fromAction {
        initRtc(context)
        this.listener = listener
        Timber.i("starting call to ${(commSocket as CustomWebSocketClient?)?.address}")
        connection = factory?.createPeerConnection(emptyList(), constraints, object : DefaultObserver() {
            override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState?) {
                if (iceGatheringState == PeerConnection.IceGatheringState.COMPLETE) {
                    onIceGatheringComplete()
                    reportStateChange(CallState.CONNECTING)
                }
            }

            override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
                Timber.i("onIceConnectionChange ${iceConnectionState?.name}")
                if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    reportStateChange(CallState.ENDED)
                }
                if (iceConnectionState == PeerConnection.IceConnectionState.COMPLETED) {
                    Timber.e("Handled state compleated. probably video has been freezed")
                    reportStateChange(CallState.COMPLETED)
                }
            }

            override fun onAddStream(mediaStream: MediaStream?) {
                mediaStream?.let(::handleMediaStream)
            }

            override fun onDataChannel(dataChannel: DataChannel?) {
                this@RtcClient.dataChannel = dataChannel
                dataChannel?.registerObserver(dataChannelObserver)
            }
        })
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
