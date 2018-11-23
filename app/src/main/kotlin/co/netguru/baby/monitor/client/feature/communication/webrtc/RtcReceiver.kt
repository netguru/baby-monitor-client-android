package co.netguru.baby.monitor.client.feature.communication.webrtc

import android.content.Context
import io.reactivex.Completable
import org.java_websocket.WebSocket
import org.json.JSONObject
import org.webrtc.*
import timber.log.Timber
import java.nio.charset.Charset

class RtcReceiver(commSocket: WebSocket, offer: String) : RtcCall() {

    init {
        this.commSocket = commSocket
        this.offer = offer
    }

    fun accept(context: Context, listener: (state: CallState) -> Unit) = Completable.fromAction {
        initRtc(context)
        this.listener = listener
        connection = factory?.createPeerConnection(emptyList(), constraints, object : DefaultObserver() {
            override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState?) {
                if (iceGatheringState == PeerConnection.IceGatheringState.COMPLETE) {
                    transferAnswer()
                }
            }

            override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
                if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    reportStateChange(CallState.ENDED)
                }
            }

            override fun onAddStream(mediaStream: MediaStream?) {
                mediaStream?.let(::handleMediaStream)
            }

            override fun onDataChannel(dataChannel: DataChannel?) {
                this@RtcReceiver.dataChannel = dataChannel
                dataChannel?.registerObserver(dataChannelObserver)
            }
        })
        connection?.addStream(createStream())
        setRemoteDescription()
    }

    override fun createStream(): MediaStream? {
        upStream = factory?.createLocalMediaStream(LOCAL_MEDIA_STREAM_LABEL)
        audio = factory?.createAudioTrack(AUDIO_TRACK_ID, factory?.createAudioSource(MediaConstraints()))
        upStream?.addTrack(audio)
        videoTrack = createVideoTrack()
        upStream?.addTrack(videoTrack)
        capturer?.startCapture(500, 500, 30)
        return upStream
    }

    private fun setRemoteDescription() {
        connection?.setRemoteDescription(
                DefaultSdpObserver(onSetSuccess = { createAnsewer() }),
                SessionDescription(SessionDescription.Type.OFFER, offer)
        )
    }

    private fun createAnsewer() {
        connection?.createAnswer(DefaultSdpObserver(
                onCreateSuccess = { sessionDescription ->
                    connection?.setLocalDescription(
                            DefaultSdpObserver(),
                            sessionDescription
                    )
                },
                onCreateFailure = { error ->
                    Timber.e("creation failed: $error")
                }
        ), constraints)
    }

    private fun transferAnswer() {
        Timber.i("transferring answer")
        val jsonObject = JSONObject().apply {
            put(
                    P2P_ANSWER,
                    JSONObject().apply {
                        put("sdp", connection?.localDescription?.description)
                        put("type", connection?.localDescription?.type?.canonicalForm())
                    }
            )
        }
        commSocket?.send(jsonObject.toString().toByteArray(Charset.defaultCharset()))
        reportStateChange(CallState.CONNECTED)
    }
}
