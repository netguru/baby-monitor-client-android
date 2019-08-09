package co.netguru.baby.monitor.client.feature.communication.webrtc

import android.content.Context
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultObserver
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultSdpObserver
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import org.webrtc.*
import timber.log.Timber

class WebRtcManager constructor(
    private val sendMessage: (Message) -> Unit
) {

    private lateinit var peerConnectionFactory: PeerConnectionFactory

    private lateinit var videoCapturer: VideoCapturer
    private lateinit var videoSource: VideoSource
    private lateinit var videoTrack: VideoTrack
    private lateinit var audioSource: AudioSource
    private lateinit var audioTrack: AudioTrack

    private lateinit var peerConnection: PeerConnection

    private val sharedContext by lazy { EglBase.create().eglBaseContext }

    private val mediaConstraints = MediaConstraints().apply {
        mandatory.addAll(
            arrayOf(
                MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"),
                MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
            )
        )
    }

    private fun createCameraCapturer(cameraEnumerator: CameraEnumerator) =
        cameraEnumerator.deviceNames.asSequence()
            // prefer back-facing cameras
            .sortedBy { deviceName ->
                if (cameraEnumerator.isBackFacing(deviceName)) 1
                else 2
            }
            .mapNotNull { deviceName ->
                cameraEnumerator.createCapturer(deviceName, null)
            }
            .first()

    fun beginCapturing(context: Context) {
        Timber.d("beginCapturing()")

        Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE)
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )
        peerConnectionFactory = PeerConnectionFactory(PeerConnectionFactory.Options()).apply {
            setVideoHwAccelerationOptions(sharedContext, sharedContext)
        }
        videoCapturer = createCameraCapturer(Camera2Enumerator(context))
        videoSource = peerConnectionFactory.createVideoSource(videoCapturer)
        videoTrack = peerConnectionFactory.createVideoTrack("video", videoSource)
        audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        audioTrack = peerConnectionFactory.createAudioTrack("audio", audioSource)
        videoCapturer.startCapture(320, 480, 30)

        peerConnection = peerConnectionFactory.createPeerConnection(
            emptyList(),
            mediaConstraints,
            object : DefaultObserver() {
                override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState?) {
                    Timber.d("onIceGatheringChange($iceGatheringState)")
                    if (iceGatheringState == PeerConnection.IceGatheringState.COMPLETE)
                        transferAnswer()
                }

                override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
                    Timber.d("onIceConnectionChange($iceConnectionState)")
                }
            }
        )

        val stream = peerConnectionFactory.createLocalMediaStream("stream")
        stream.addTrack(audioTrack)
        stream.addTrack(videoTrack)
        peerConnection.addStream(stream)
    }

    fun stopCapturing() {
        Timber.d("stopCapturing()")
        videoCapturer.stopCapture()
        audioTrack.dispose()
        audioSource.dispose()
        videoTrack.dispose()
        videoSource.dispose()
        videoCapturer.dispose()
        peerConnection.dispose()
    }

    fun acceptOffer(offer: String) {
        Timber.i("acceptOffer($offer)")
        peerConnection.setRemoteDescription(DefaultSdpObserver(
            onSetSuccess = {
                answer()
            }
        ), SessionDescription(SessionDescription.Type.OFFER, offer))
    }

    private fun answer() {
        Timber.i("answer()")
        peerConnection.createAnswer(DefaultSdpObserver(
            onCreateSuccess = { sessionDescription ->
                Timber.d("Successfully created answer.")
                peerConnection.setLocalDescription(
                    DefaultSdpObserver(),
                    sessionDescription
                )
            },
            onCreateFailure = { error ->
                Timber.w("Creating answer failed.")
            }
        ), mediaConstraints)
    }

    private fun transferAnswer() {
        Timber.i("Transferring answer.")
        sendMessage(
            Message(
                sdpAnswer = Message.SdpData(
                    sdp = peerConnection.localDescription?.description,
                    type = peerConnection.localDescription?.type?.canonicalForm()
                )
            )
        )
    }

    fun addSurfaceView(surfaceViewRenderer: SurfaceViewRenderer) {
        surfaceViewRenderer.init(sharedContext, null)
        videoTrack.addSink(surfaceViewRenderer)
    }
}
