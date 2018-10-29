package co.netguru.baby.monitor.client.feature.server.player

import android.content.Context
import android.net.Uri
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.client.home.lullabies.LullabyData
import co.netguru.baby.monitor.client.feature.websocket.Action
import co.netguru.baby.monitor.client.feature.websocket.LullabyCommand
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Single
import javax.inject.Inject

class LullabyPlayer @Inject constructor(
        private val context: Context
) {
    internal var playbackEvents: PlaybackEvents? = null
    private lateinit var lastCommand: LullabyCommand
    private val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
    private val exoPlayer: SimpleExoPlayer

    init {
        val defaultRenderers = DefaultRenderersFactory(
                context,
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
        )
        exoPlayer = ExoPlayerFactory.newSimpleInstance(defaultRenderers, DefaultTrackSelector())
        exoPlayer.addListener(object : PlayerEventListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        playbackEvents?.onLullabyEnded(LullabyCommand(lastCommand.lullabyName, Action.STOP))
                    }
                }
            }
        })
    }

    internal fun handleActionRequest(
            command: LullabyCommand
    ): Single<LullabyCommand>? {
        lastCommand = command
        return when (command.action) {
            Action.PLAY -> play(command.lullabyName, command)
            Action.RESUME -> handlePlayback(true, command)
            Action.PAUSE -> handlePlayback(false, command)
            Action.REPEAT -> TODO("29.10.2018")
            Action.STOP -> stopPlayback(command)
        }
    }


    private fun play(title: String, command: LullabyCommand): Single<LullabyCommand>? {
        val lullaby = lullabies.find { it.name == title }
        return if (lullaby != null) {
            playLullabyFromAssets(lullaby, command)
        } else {
            null
        }
    }

    private fun playLullabyFromAssets(
            lullaby: LullabyData,
            command: LullabyCommand
    ) = Single.fromCallable {
        val mediaSource = ExtractorMediaSource
                .Factory(DefaultDataSourceFactory(context, userAgent))
                .createMediaSource(Uri.parse("asset:///${lullaby.name}.mp3"))
        exoPlayer.prepare(mediaSource)
        exoPlayer.playWhenReady = true
        return@fromCallable command
    }

    private fun handlePlayback(shouldPlay: Boolean, command: LullabyCommand) =
            Single.fromCallable {
                exoPlayer.playWhenReady = shouldPlay
                return@fromCallable command
            }

    private fun stopPlayback(command: LullabyCommand) = Single.fromCallable {
        exoPlayer.stop()
        exoPlayer.seekTo(0)
        return@fromCallable command
    }

    internal fun clear() {
        exoPlayer.release()
    }

    interface PlaybackEvents {
        fun onLullabyEnded(command: LullabyCommand)
    }

    companion object {
        /**
         * list of lullabies in assets
         */
        internal val lullabies = listOf(
                LullabyData.LullabyHeader("BM library"),
                LullabyData.LullabyInfo("test", "00:10"),
                LullabyData.LullabyInfo("Hush little baby", "01:16"),
                LullabyData.LullabyInfo("Lullaby goodnight", "02:31"),
                LullabyData.LullabyInfo("Pretty little horses", "01:34"),
                LullabyData.LullabyInfo("Rock a bye baby", "2:33"),
                LullabyData.LullabyInfo("Twinkle twinkle little star", "2:33"),
                LullabyData.LullabyHeader("User library")
        )
    }
}
