package co.netguru.babymonitorclient

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import kotlinx.android.synthetic.main.activity_main.*
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var libvlc: LibVLC
    private lateinit var mediaPlayer: MediaPlayer

    private fun provideOptions() = mutableListOf<String>().apply {
        add("--aout=opensles")
        add("--audio-time-stretch") // time stretching
        add("-vvv") //verbosity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        if (SHOULD_PLAY_RTMP) {
            prepareRtmpPlayer()
        } else {
            libvlc = LibVLC(this, provideOptions() as ArrayList<String>)
            mediaPlayer = MediaPlayer(libvlc)
            prepareRtspPlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun prepareRtmpPlayer() {
        showSurfaceView(false)
        val player = ExoPlayerFactory.newSimpleInstance(this, provideTrackSelector())
        simplePlayer.player = player

        with(player) {
            prepare(provideVideoSource())
            playWhenReady = true
        }
    }

    private fun provideTrackSelector() =
        DefaultTrackSelector(AdaptiveTrackSelection.Factory(DefaultBandwidthMeter()))

    private fun provideVideoSource() = ExtractorMediaSource.Factory(RtmpDataSourceFactory())
        .createMediaSource(Uri.parse(RTMP_VIDEO_URI))

    private fun showSurfaceView(isVisible: Boolean) {
        surfaceView.visibility = if (isVisible) View.VISIBLE else View.GONE
        simplePlayer.visibility = if (isVisible) View.GONE else View.VISIBLE
    }

    private fun prepareRtspPlayer() {
        showSurfaceView(true)

        surfaceView.holder.setKeepScreenOn(true)
        // Seting up video output
        with(mediaPlayer.vlcVout) {
            setVideoView(surfaceView)
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            with(displayMetrics) {
                setWindowSize(widthPixels, heightPixels)
            }
            attachViews()
        }

        with(mediaPlayer) {
            this.media = Media(libvlc, Uri.parse(RTSP_VIDEO_URI)).apply {
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

    companion object {
        private const val SHOULD_PLAY_RTMP = false
        private const val RTMP_VIDEO_URI =
            "rtmp://stream1.livestreamingservices.com:1935/tvmlive/tvmlive"

        private const val RTSP_VIDEO_URI = "rtsp://192.168.0.100:5006"
    }
}
