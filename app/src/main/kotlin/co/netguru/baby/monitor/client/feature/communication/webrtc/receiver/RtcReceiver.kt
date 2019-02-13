package co.netguru.baby.monitor.client.feature.communication.webrtc.receiver

import android.content.Context
import android.os.Handler
import android.os.Looper
import co.netguru.baby.monitor.client.common.view.CustomSurfaceViewRenderer
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcCall
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultObserver
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultSdpObserver
import io.reactivex.Completable
import org.java_websocket.WebSocket
import org.json.JSONObject
import org.webrtc.*
import org.webrtc.PeerConnection.IceConnectionState
import org.webrtc.PeerConnection.IceGatheringState
import timber.log.Timber
import java.nio.charset.Charset

class RtcReceiver(
        context: Context,
        var localView: CustomSurfaceViewRenderer? = null,
        listener: (state: CallState) -> Unit
) : RtcCall() {

    init {
        initRtc(context)
        this.listener = listener
        localView?.let { view ->
            if (!view.initialized) {
                view.init(sharedContext, null)
            }
        }
        recreateVideoTrack()
    }

    fun recreateVideoTrack(isFacingFront: Boolean = false, cameraChangedListener: (() -> Unit) = {}) {
        capturer?.stopCapture()
        capturer?.dispose()
        localView?.addFrameListener({
            cameraChangedListener()
            localView?.post {
                localView?.removeFrameListener { cameraChangedListener() }
            }
        }, SURFACE_VIEW_FRAME_SCALE)

        videoTrack = createVideoTrack(isFacingFront)
        capturer?.startCapture(VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_FPS)
        Handler(Looper.getMainLooper()).post {
            val localVideoRenderer = VideoRenderer(localView)
            videoTrack?.addRenderer(localVideoRenderer)
        }
        if (state == CallState.CONNECTED) {
            transferAnswer()
        }
    }

    fun accept(
            commSocket: WebSocket,
            offer: String
    ) = Completable.fromAction {
        listener(CallState.CONNECTING)
        this.commSocket = commSocket
        this.offer = offer

        connection = factory?.createPeerConnection(emptyList(), constraints, object : DefaultObserver() {
            override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState?) {
                if (iceGatheringState == IceGatheringState.COMPLETE) {
                    transferAnswer()
                }
            }

            override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
                when (iceConnectionState) {
                    IceConnectionState.DISCONNECTED -> reportStateChange(CallState.ENDED)
                    IceConnectionState.FAILED -> reportStateChange(CallState.ERROR)
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

    fun stopCall() = Completable.fromAction {
        if (commSocket?.isOpen == true) {
            commSocket?.send(
                    JSONObject().apply {
                        put(WEB_SOCKET_ACTION_KEY, "dismissed")
                    }.toString()
            )
        }
        connection?.close()
        audioSource?.dispose()
    }

    fun startCapturer() {
        capturer?.startCapture(VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_FPS)
    }

    override fun createStream(): MediaStream? {
        upStream = factory?.createLocalMediaStream(LOCAL_MEDIA_STREAM_LABEL)
        audioSource = factory?.createAudioSource(MediaConstraints())
        audio = factory?.createAudioTrack(AUDIO_TRACK_ID, audioSource)
        upStream?.addTrack(audio)
        upStream?.addTrack(videoTrack)
        return upStream
    }

    private fun setRemoteDescription() {
        connection?.setRemoteDescription(
                DefaultSdpObserver(onSetSuccess = { createAnswer() }),
                SessionDescription(SessionDescription.Type.OFFER, offer)
        )
    }

    private fun createAnswer() {
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
        if (commSocket?.isOpen == true) {
            commSocket?.send(jsonObject.toString().toByteArray(Charset.defaultCharset()))
        }
        reportStateChange(CallState.CONNECTED)
    }

    companion object {
        private const val SURFACE_VIEW_FRAME_SCALE = 1f
    }
}
