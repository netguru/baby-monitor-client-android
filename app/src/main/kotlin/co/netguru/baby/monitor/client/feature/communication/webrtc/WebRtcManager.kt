package co.netguru.baby.monitor.client.feature.communication.webrtc

import android.content.Context
import org.webrtc.*
import timber.log.Timber
import javax.inject.Inject

class WebRtcManager @Inject constructor() {
    private lateinit var peerConnectionFactory: PeerConnectionFactory

    private lateinit var videoCapturer: VideoCapturer
    private lateinit var videoSource: VideoSource
    private lateinit var videoTrack: VideoTrack
    private lateinit var audioSource: AudioSource
    private lateinit var audioTrack: AudioTrack

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
        peerConnectionFactory = PeerConnectionFactory(PeerConnectionFactory.Options())
        videoCapturer = createCameraCapturer(Camera2Enumerator(context))
        videoSource = peerConnectionFactory.createVideoSource(videoCapturer)
        videoTrack = peerConnectionFactory.createVideoTrack("video", videoSource)
        val mediaConstraints = MediaConstraints()
        audioSource = peerConnectionFactory.createAudioSource(mediaConstraints)
        audioTrack = peerConnectionFactory.createAudioTrack("audio", audioSource)
        videoCapturer.startCapture(320, 480, 30)
    }

    fun stopCapturing() {
        Timber.d("stopCapturing()")
        videoCapturer.stopCapture()
        audioTrack.dispose()
        audioSource.dispose()
        videoTrack.dispose()
        videoSource.dispose()
        videoCapturer.dispose()
    }

    fun addSurfaceView(surfaceViewRenderer: SurfaceViewRenderer) {
        videoTrack.addSink(surfaceViewRenderer)
    }
}
