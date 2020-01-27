package co.netguru.baby.monitor.client.common.extensions

import android.app.Activity
import android.media.AudioManager

fun Activity.controlVideoStreamVolume() {
    volumeControlStream = AudioManager.STREAM_VOICE_CALL
}
