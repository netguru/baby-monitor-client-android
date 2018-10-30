package co.netguru.baby.monitor.client.feature.server.player

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

open class PlayerEventListener : Player.EventListener {
    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) = Unit
    override fun onSeekProcessed() = Unit
    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) = Unit
    override fun onPlayerError(error: ExoPlaybackException?) = Unit
    override fun onLoadingChanged(isLoading: Boolean) = Unit
    override fun onPositionDiscontinuity(reason: Int) = Unit
    override fun onRepeatModeChanged(repeatMode: Int) = Unit
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = Unit
    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) = Unit
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) = Unit
}
