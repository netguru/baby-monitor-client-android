package co.netguru.babymonitorserver

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Size
import android.view.Surface
import net.majorkernelpanic.streaming.Session
import net.majorkernelpanic.streaming.SessionBuilder
import net.majorkernelpanic.streaming.audio.AudioQuality
import net.majorkernelpanic.streaming.gl.SurfaceView
import net.majorkernelpanic.streaming.video.VideoQuality
import timber.log.Timber
import kotlin.math.absoluteValue

object Utils {

    private const val SAMPLING_RATE = 8000
    private const val AUDIO_BIT_RATE = 16000

    private const val FRAME_RATE = 30
    private const val VIDEO_BIT_RATE = 1_000

    fun buildService(
            surfaceView: SurfaceView,
            activity: Activity,
            sessionCallback: Session.Callback
    ): Session {
        val size = getBestResolution(activity, VideoQuality.P_480)
        Timber.e(size.toString())
        return SessionBuilder.getInstance()
                .setCallback(sessionCallback)
                .setSurfaceView(surfaceView)
                .setPreviewOrientation(getCameraOrientation(activity))
                .setContext(activity.applicationContext)
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setAudioQuality(AudioQuality(SAMPLING_RATE, AUDIO_BIT_RATE))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(VideoQuality(size.width, size.height, FRAME_RATE, VIDEO_BIT_RATE))
                .build()
    }

    private fun getCameraOrientation(activity: Activity): Int {
        val degrees = when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }

        var sensorOrientation = 0
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (id in manager.cameraIdList) {
            val characteristics = manager.getCameraCharacteristics(id)
            if (characteristics[CameraCharacteristics.LENS_FACING]
                    == CameraCharacteristics.LENS_FACING_FRONT) {

                val temp = (characteristics[CameraCharacteristics.SENSOR_ORIENTATION] + degrees) % 360
                sensorOrientation = (360 - temp) % 360
            }
        }
        return sensorOrientation
    }

    private fun getBestResolution(context: Context, size: Size): Size {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (id in manager.cameraIdList) {
            val characteristics = manager.getCameraCharacteristics(id)
            if (characteristics[CameraCharacteristics.LENS_FACING]
                    == CameraCharacteristics.LENS_FACING_FRONT) {
                val sizeList = characteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(SurfaceTexture::class.java)
                        .toList()
                        .sortedBy { it.width }
                        .also { Timber.e(it.toString()) }

                with(sizeList) {
                    find { it.width == size.width && it.height == size.height }
                            ?.let { return it }

                    filter { it.width == size.width }
                            .minBy { (it.height - size.height).absoluteValue }
                            ?.let { return it }

                    filter { it.height == size.height }
                            .minBy { (it.width - size.width).absoluteValue }
                            ?.let { return it }
                }
                return sizeList.find { it.width <= size.width && it.height <= size.height }
                        ?: continue
            }
        }
        val defaultQuality = VideoQuality.DEFAULT_VIDEO_QUALITY
        return Size(defaultQuality.resX, defaultQuality.resY)
    }

}