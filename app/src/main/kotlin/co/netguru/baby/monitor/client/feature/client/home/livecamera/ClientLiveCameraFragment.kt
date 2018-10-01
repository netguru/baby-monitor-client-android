package co.netguru.baby.monitor.client.feature.client.home.livecamera

import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_client.*
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ClientLiveCameraFragment : DaggerFragment() {

    @Inject
    internal lateinit var configurationRepository: ConfigurationRepository

    private lateinit var libvlc: LibVLC
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libvlc = LibVLC(requireContext(), provideOptions() as ArrayList<String>)
        mediaPlayer = MediaPlayer(libvlc)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_client, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareRtspPlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }

    private fun provideOptions() = mutableListOf<String>().apply {
        add("--aout=opensles")
        add("--audio-time-stretch") // time stretching
        add("-vvv") //verbosity
        add("--video-filter=transform")
        add("--transform-type=90")
    }

    private fun prepareRtspPlayer() {
        // Seting up video output
        with(mediaPlayer.vlcVout) {
            setVideoView(surfaceView)
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            with(displayMetrics) {
                setWindowSize(widthPixels, heightPixels)
            }
            attachViews()
        }
        val adrress = configurationRepository.serverAddress
        Timber.e(adrress)
        with(mediaPlayer) {
            this.media = Media(
                    libvlc, Uri.parse(adrress)
            ).apply {
                setHWDecoderEnabled(true, false)
                addOption(":network-caching=150")
                addOption(":clock-jitter=0")
                addOption(":clock-synchro=0")
                addOption(":fullscreen")
            }
            play()
        }
    }

    private fun releasePlayer() = with(mediaPlayer) {
        stop()
        vlcVout.detachViews()
        libvlc.release()
    }
}
