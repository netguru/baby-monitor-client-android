package co.netguru.baby.monitor.client.feature.server.player

import android.content.Context
import android.net.Uri
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.client.home.lullabies.LullabyData
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Completable
import javax.inject.Inject

class LullabyPlayer @Inject constructor(
        private val context: Context
) {
    private val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
    private val exoPlayer: SimpleExoPlayer

    init {
        val defaultRenderers = DefaultRenderersFactory(
                context,
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
        )
        exoPlayer = ExoPlayerFactory.newSimpleInstance(defaultRenderers, DefaultTrackSelector())
    }

    fun play(title: String): Completable? {
        val lullaby = lullabies.find { it.name == title }
        return if (lullaby != null) {
            playLullabyFromAssets(lullaby)
        } else {
            null
        }
    }

    private fun playLullabyFromAssets(
            lullaby: LullabyData
    ) = Completable.fromAction {
        val mediaSource = ExtractorMediaSource
                .Factory(DefaultDataSourceFactory(context, userAgent))
                .createMediaSource(Uri.parse("asset:///${lullaby.name}.mp3"))
        exoPlayer.prepare(mediaSource)
        exoPlayer.playWhenReady = true
    }

    internal fun clear() {
        exoPlayer.release()
    }

    companion object {
        /**
         * list of lullabies in assets
         */
        internal val lullabies = listOf(
                LullabyData.LullabyHeader("BM library"),
                LullabyData.LullabyInfo("Hush little baby", "01:16"),
                LullabyData.LullabyInfo("Lullaby goodnight", "02:31"),
                LullabyData.LullabyInfo("Pretty little horses", "01:34"),
                LullabyData.LullabyInfo("Rock a bye baby", "2:33"),
                LullabyData.LullabyInfo("Twinkle twinkle little star", "2:33"),
                LullabyData.LullabyHeader("User library")
        )
    }
}
